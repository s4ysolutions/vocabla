package solutions.s4y.vocabla.endpoint.http

import org.slf4j.LoggerFactory
import solutions.s4y.vocabla.app.VocablaApp
import solutions.s4y.vocabla.app.ports.PingUseCase
import solutions.s4y.vocabla.endpoint.http.rest.Ping
import zio.http.*
import zio.http.Middleware.CorsConfig
import zio.http.endpoint.openapi.{OpenAPI, OpenAPIGen, SwaggerUI}
import zio.{LogLevel, Promise, ZEnvironment, ZIO, ZLayer}

import scala.language.postfixOps

final class RESTService(
    private val server: Server,
    private val useCases: PingUseCase
):
  RESTService.logger.debug("Creating RESTService instance")

  private val endpoints =
    Seq(Ping.endpoint)

  private val openAPI: OpenAPI = OpenAPIGen.fromEndpoints(
    title = "Vocabla API",
    version = "1.0.0",
    endpoints
  )

  private val restRoutes: Seq[Route[PingUseCase, Response]] =
    Seq(Ping.route) // , Entries.route)

  private val corsConfig: CorsConfig = CorsConfig()
  private val routes: Routes[PingUseCase, Response] =
    (Routes.fromIterable(restRoutes)
      @@ Middleware.cors(
        corsConfig
      ) ++ SwaggerUI.routes(
        "/openapi",
        openAPI
      )) @@ Middleware.requestLogging(level =
      status =>
        if (status.isSuccess) {
          LogLevel.Info
        } else {
          LogLevel.Error
        }
    )

  def start(): ZIO[
    Any,
    Nothing,
    Promise[Nothing, Unit]
  ] = (for {
    promise <- Promise.make[Nothing, Unit]
    _ <- for {
      _ <- server.install(routes)
      port <- server.port
      _ <- ZIO.log(s"Server started on http://localhost: $port")
      _ <- promise.succeed(())
      _ <- ZIO.never.fork
    } yield ()
  } yield promise).provideEnvironment(ZEnvironment(useCases))
end RESTService

object RESTService:
  val layer: ZLayer[Any, String, RESTService] = {
    ZLayer.succeed("Constructing RESTService layer") >>>
      VocablaApp.layer.flatMap(app =>
        ZLayer.succeed(ZIO.logDebug("Constructing HTTP server...")) >>>
          Server.default
            .mapError(th => th.getMessage)
            .map(server => app ++ server)
      )
      >>> ZLayer.fromFunction(
        new RESTService(_, _)
      )
  }

  private val logger = LoggerFactory.getLogger(RESTService.getClass)
