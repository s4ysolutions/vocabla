package solutions.s4y.vocabla.endpoint.http

import org.slf4j.LoggerFactory
import solutions.s4y.vocabla.app.VocablaApp
import solutions.s4y.vocabla.app.ports.*
import solutions.s4y.vocabla.domain.UserContext
import solutions.s4y.vocabla.domain.identity.IdentifierSchema
import solutions.s4y.vocabla.endpoint.http.rest.Ping
import solutions.s4y.vocabla.endpoint.http.rest.middleware.Authentication.bearerAuthWithContext
import solutions.s4y.vocabla.endpoint.http.rest.tags.{CreateTag, GetTag}
import solutions.s4y.vocabla.endpoint.http.rest.words.{CreateEntry, GetEntry}
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
      CreateEntry.endpoint,
      CreateTag.endpoint,
      GetEntry.endpoint,
      GetTag.endpoint
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
          CreateEntry.route,
          CreateTag.route,
          GetEntry.route,
          GetTag.route
        ) @@ bearerAuthWithContext) @@ Middleware.cors(corsConfig)
      ++ SwaggerUI.routes("/openapi", openAPI) @@ Middleware
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
  val layer: ZLayer[Any, String, RESTService] = {
    ZLayer.succeed("Constructing RESTService layer") >>>
      VocablaApp.layer.flatMap(app =>
        ZLayer.succeed(ZIO.logDebug("Constructing HTTP server...")) >>>
          Server.default
            .mapError(th => th.getMessage)
            .map(server => app ++ server)
      ) >>> ZLayer.fromZIO(for {
        server <- ZIO.service[Server]
        pingUseCase <- ZIO.service[PingUseCase]
        getUserUseCase <- ZIO.service[GetUserUseCase]
        createEntryUseCase <- ZIO.service[CreateEntryUseCase]
        createTagUseCase <- ZIO.service[CreateTagUseCase]
        getEntryUseCase <- ZIO.service[GetEntryUseCase]
        getTagUseCase <- ZIO.service[GetTagUseCase]
        restService = new RESTService(
          server,
          pingUseCase,
          getUserUseCase,
          createEntryUseCase,
          createTagUseCase,
          getEntryUseCase,
          getTagUseCase
        )
        _ <- ZIO.logInfo("RESTService layer constructed")
      } yield restService)
  }

  private val logger = LoggerFactory.getLogger(RESTService.getClass)
