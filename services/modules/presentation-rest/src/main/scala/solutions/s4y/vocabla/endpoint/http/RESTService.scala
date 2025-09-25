package solutions.s4y.vocabla.endpoint.http

import org.slf4j.LoggerFactory
import solutions.s4y.i18n.ResourcesStringsResolver.default
import solutions.s4y.i18n.t
import solutions.s4y.vocabla.app.ports.*
import solutions.s4y.vocabla.app.ports.entries_get.GetEntriesUseCase
import solutions.s4y.vocabla.app.ports.entry_create.CreateEntryUseCase
import solutions.s4y.vocabla.app.ports.entry_get.GetEntryUseCase
import solutions.s4y.vocabla.app.ports.lang_get.GetLanguagesUseCase
import solutions.s4y.vocabla.app.ports.students.ls.GetLearningSettingsUseCase
import solutions.s4y.vocabla.app.ports.students.ls.tags.{CreateTagUseCase, DeleteTagUseCase, GetTagUseCase}
import solutions.s4y.vocabla.app.repo.error.InfraFailure
import solutions.s4y.vocabla.domain.identity.IdentifierSchema
import solutions.s4y.vocabla.endpoint.http.middleware.BearerUserContext.bearerAuthWithContext
import solutions.s4y.vocabla.endpoint.http.middleware.BrowserLocale.browserLocale
import solutions.s4y.vocabla.endpoint.http.routes.Ping
import solutions.s4y.vocabla.endpoint.http.routes.entries.{
  CreateEntry,
  GetEntries,
  GetEntry
}
import solutions.s4y.vocabla.endpoint.http.routes.languages.GetLanguages
import solutions.s4y.vocabla.endpoint.http.routes.students.settings.GetSettings
import solutions.s4y.vocabla.endpoint.http.routes.students.settings.tags.{
  CreateTag,
  DeleteTag,
  GetTag
}
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
    private val deleteTagUseCase: DeleteTagUseCase,
    private val getEntryUseCase: GetEntryUseCase,
    private val getEntriesUseCase: GetEntriesUseCase,
    private val getTagUseCase: GetTagUseCase,
    private val getLanguagesUseCase: GetLanguagesUseCase,
    private val getLearningSettingsUseCase: GetLearningSettingsUseCase
)(using IdentifierSchema):
  RESTService.logger.debug("Creating RESTService instance")

  private val endpoints =
    Seq(
      Ping.endpoint,
      CreateTag.endpoint,
      GetTag.endpoint,
      DeleteTag.endpoint,
      CreateEntry.endpoint,
      GetEntry.endpoint,
      GetEntries.endpoint,
      GetLanguages.endpoint,
      GetSettings.endpoint
    )

  private val openAPI: OpenAPI = OpenAPIGen.fromEndpoints(
    title = "Vocabla API",
    version = "1.0.0",
    endpoints
  )

  private val corsConfig: CorsConfig = CorsConfig()

  // Root page handler that shows API documentation links
  private val rootHandler: Handler[Any, Nothing, Request, Response] =
    Handler.fromFunction { _ =>
      val html = """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Vocabla API</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 800px;
            margin: 50px auto;
            padding: 20px;
            background-color: #f5f5f5;
        }
        .container {
            background: white;
            padding: 40px;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        h1 { color: #333; text-align: center; }
        .links { margin: 30px 0; }
        .link-item {
            display: block;
            padding: 15px;
            margin: 10px 0;
            background: #007bff;
            color: white;
            text-decoration: none;
            border-radius: 5px;
            text-align: center;
            transition: background-color 0.3s;
        }
        .link-item:hover { background: #0056b3; }
        .description { color: #666; margin: 20px 0; text-align: center; }
    </style>
</head>
<body>
    <div class="container">
        <h1>Vocabla API</h1>
        <p class="description">
            Welcome to the Vocabla vocabulary learning API.
            Use the links below to explore the API documentation and endpoints.
        </p>
        <div class="links">
            <a href="/swagger-ui" class="link-item">
                📚 API Documentation (Swagger UI)
            </a>
            <a href="/rest/v1/ping?message=hello" class="link-item">
                🏓 Test Ping Endpoint
            </a>
        </div>
        <p class="description">
            <small>API Version: 1.0.0</small>
        </p>
    </div>
</body>
</html>"""

      Response(
        status = Status.Ok,
        headers = Headers(Header.ContentType(MediaType.text.html)),
        body = Body.fromString(html)
      )
    }

  private val routes: Routes[
    PingUseCase & GetUserUseCase & CreateEntryUseCase & CreateTagUseCase &
      GetEntryUseCase & GetEntriesUseCase & GetTagUseCase & DeleteTagUseCase &
      GetLanguagesUseCase & GetLearningSettingsUseCase,
    Response
  ] = {
    // Root route for API documentation
    Routes(Method.GET / "" -> rootHandler) ++
      (Routes(Ping.route, GetLanguages.route)
        ++
          Routes(
            CreateTag.route,
            GetTag.route,
            DeleteTag.route,
            CreateEntry.route,
            GetEntry.route,
            GetEntries.route,
            GetSettings.route
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
        .add(createTagUseCase)
        .add(deleteTagUseCase)
        .add(getTagUseCase)
        .add(createEntryUseCase)
        .add(getEntryUseCase)
        .add(getEntriesUseCase)
        .add(getLanguagesUseCase)
        .add(getLearningSettingsUseCase)
    )
end RESTService

object RESTService:
  private val httpServerLayer: ZLayer[RestConfig, InfraFailure, Server] =
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
      GetEntriesUseCase & CreateTagUseCase & GetTagUseCase & DeleteTagUseCase &
      GetLanguagesUseCase & GetLearningSettingsUseCase,
    InfraFailure,
    RESTService
  ] =
    RestConfig.layer
      >>> httpServerLayer
      >>> ZLayer.fromFunction(new RESTService(_, _, _, _, _, _, _, _, _, _, _))

  private val logger = LoggerFactory.getLogger(RESTService.getClass)
