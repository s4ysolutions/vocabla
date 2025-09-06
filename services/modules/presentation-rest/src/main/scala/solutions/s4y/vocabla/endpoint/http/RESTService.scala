package solutions.s4y.vocabla.endpoint.http

import org.slf4j.LoggerFactory
import solutions.s4y.i18n.ResourcesStringsResolver.default
import solutions.s4y.i18n.t
import solutions.s4y.vocabla.app.ports.*
import solutions.s4y.vocabla.app.ports.tag_create.CreateTagUseCase
import solutions.s4y.vocabla.app.repo.error.InfraFailure
import solutions.s4y.vocabla.domain.identity.IdentifierSchema
import solutions.s4y.vocabla.endpoint.http.middleware.BearerUserContext.bearerAuthWithContext
import solutions.s4y.vocabla.endpoint.http.middleware.BrowserLocale.browserLocale
import solutions.s4y.vocabla.endpoint.http.routes.Ping
import solutions.s4y.vocabla.endpoint.http.routes.entries.{CreateEntry, GetEntry}
import solutions.s4y.vocabla.endpoint.http.routes.tags.{CreateTag, GetTag}
import solutions.s4y.vocabla.endpoint.http.schema.given
import zio.http.*
import zio.http.Middleware.CorsConfig
import zio.http.endpoint.openapi.{OpenAPI, OpenAPIGen, SwaggerUI}
import zio.{LogLevel, Promise, ZEnvironment, ZIO, ZLayer}

import scala.language.postfixOps

final class RESTService(
    private val server: Server,
    private val pingUseCase: PingUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val createEntryUseCase: CreateEntryUseCase,
    private val createTagUseCase: CreateTagUseCase,
    private val getEntryUseCase: GetEntryUseCase,
    private val getTagUseCase: GetTagUseCase
)(using IdentifierSchema):
  RESTService.logger.debug("Creating RESTService instance")

  private val endpoints =
    Seq(
      Ping.endpoint,
      CreateTag.endpoint,
      GetTag.endpoint,
      CreateEntry.endpoint,
      GetEntry.endpoint
    )

  private val openAPI: OpenAPI = OpenAPIGen.fromEndpoints(
    title = "Vocabla API",
    version = "1.0.0",
    endpoints
  )

  private val corsConfig: CorsConfig = CorsConfig()
  private val routes: Routes[
    PingUseCase & GetUserUseCase & CreateEntryUseCase & CreateTagUseCase &
      GetEntryUseCase & GetTagUseCase,
    Response
  ] = {
    (Routes(Ping.route)
      ++
        Routes(
          CreateTag.route,
          GetTag.route,
          CreateEntry.route,
          GetEntry.route
        ) @@ bearerAuthWithContext) @@ Middleware.cors(
      corsConfig
    ) @@ browserLocale
      ++ SwaggerUI.routes("/swagger-ui", openAPI) @@ Middleware
        .requestLogging(level =
          status =>
            if (status.isSuccess) {
              LogLevel.Info
            } else {
              LogLevel.Error
            }
        )
  }

  def start(): ZIO[
    Any,
    Nothing,
    Promise[Nothing, Unit]
  ] = (for {
    promise <- Promise.make[Nothing, Unit]
    _ <- for {
      _ <- server.install(routes)
      port <- server.port
      _ <- ZIO.log(s"Server started on http://localhost:$port")
      _ <- promise.succeed(())
      _ <- ZIO.never.fork
    } yield ()
  } yield promise)
    .provideEnvironment(
      ZEnvironment(pingUseCase)
        .add(getUserUseCase)
        .add(createEntryUseCase)
        .add(createTagUseCase)
        .add(getEntryUseCase)
        .add(getTagUseCase)
    )
end RESTService

object RESTService:
  val httpServerLayer: ZLayer[RestConfig, InfraFailure, Server] =
    ZLayer.fromZIO {
      ZIO.logDebug("Constructing HTTP server...") *>
        ZIO.serviceWith[RestConfig](restConfig =>
          Server.Config.default.port(restConfig.port)
        )
    } >>> Server.live.mapError(th =>
      InfraFailure(t"Failed to start HTTP server", th)
    )

  val layer: ZLayer[
    CreateEntryUseCase & GetUserUseCase & PingUseCase & GetEntryUseCase &
      CreateTagUseCase & GetTagUseCase,
    InfraFailure,
    RESTService
  ] =
    RestConfig.layer
      >>> httpServerLayer
      >>> ZLayer.fromFunction(new RESTService(_, _, _, _, _, _, _))

  private val logger = LoggerFactory.getLogger(RESTService.getClass)
