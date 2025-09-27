package solutions.s4y.vocabla.endpoint.http

import org.slf4j.LoggerFactory
import solutions.s4y.i18n.ResourcesStringsResolver.default
import solutions.s4y.i18n.t
import solutions.s4y.vocabla.app.ports.*
import solutions.s4y.vocabla.app.ports.entries_get.GetEntriesUseCase
import solutions.s4y.vocabla.app.ports.entry_create.CreateEntryUseCase
import solutions.s4y.vocabla.app.ports.entry_get.GetEntryUseCase
import solutions.s4y.vocabla.app.ports.lang_get.GetLanguagesUseCase
import solutions.s4y.vocabla.app.ports.students.settings.GetLearningSettingsUseCase
import solutions.s4y.vocabla.app.ports.students.settings.tags.{
  CreateTagUseCase,
  DeleteTagUseCase,
  GetTagUseCase
}
import solutions.s4y.vocabla.app.ports.students.settings.known_lang.{
  AddKnownLangUseCase,
  RemoveKnownLangUseCase
}
import solutions.s4y.vocabla.app.ports.students.settings.learn_lang.{
  AddLearnLangUseCase,
  RemoveLearnLangUseCase
}
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
import solutions.s4y.vocabla.endpoint.http.routes.students.settings.known_lang.{
  AddKnownLang,
  RemoveKnownLang
}
import solutions.s4y.vocabla.endpoint.http.routes.students.settings.learn_lang.{
  AddLearnLang,
  RemoveLearnLang
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
    private val getLearningSettingsUseCase: GetLearningSettingsUseCase,
    private val addKnownLangUseCase: AddKnownLangUseCase,
    private val removeKnownLangUseCase: RemoveKnownLangUseCase,
    private val addLearnLangUseCase: AddLearnLangUseCase,
    private val removeLearnLangUseCase: RemoveLearnLangUseCase
):
  RESTService.logger.debug("Creating RESTService instance")
  given IdentifierSchema = IdentifierSchema[Long]

  private def endpoints =
    Seq(
      Ping.endpoint,
      CreateTag.endpoint,
      GetTag.endpoint,
      DeleteTag.endpoint,
      CreateEntry.endpoint,
      GetEntry.endpoint,
      GetEntries.endpoint,
      GetLanguages.endpoint,
      GetSettings.endpoint,
      AddKnownLang.endpoint,
      RemoveKnownLang.endpoint,
      AddLearnLang.endpoint,
      RemoveLearnLang.endpoint
    )

  private val openAPI: OpenAPI = OpenAPIGen.fromEndpoints(
    title = "Vocabla API",
    version = "1.0.0",
    endpoints
  )

  private val corsConfig: CorsConfig = CorsConfig()

  private val routes: Routes[
    PingUseCase & GetUserUseCase & CreateEntryUseCase & CreateTagUseCase &
      GetEntryUseCase & GetEntriesUseCase & GetTagUseCase & DeleteTagUseCase &
      GetLanguagesUseCase & GetLearningSettingsUseCase & AddKnownLangUseCase &
      RemoveKnownLangUseCase & AddLearnLangUseCase & RemoveLearnLangUseCase,
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
            GetSettings.route,
            AddKnownLang.route,
            RemoveKnownLang.route,
            AddLearnLang.route,
            RemoveLearnLang.route
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
        .add(addKnownLangUseCase)
        .add(removeKnownLangUseCase)
        .add(addLearnLangUseCase)
        .add(removeLearnLangUseCase)
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
      GetLanguagesUseCase & GetLearningSettingsUseCase & AddKnownLangUseCase &
      RemoveKnownLangUseCase & AddLearnLangUseCase & RemoveLearnLangUseCase,
    InfraFailure,
    RESTService
  ] =
    RestConfig.layer
      >>> httpServerLayer
      >>> ZLayer.fromFunction(
        new RESTService(_, _, _, _, _, _, _, _, _, _, _, _, _, _, _)
      )

  private val logger = LoggerFactory.getLogger(RESTService.getClass)
