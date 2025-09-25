package solutions.s4y.vocabla.endpoint.http.routes.students.settings

import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.app.ports.students.settings.{GetLearningSettingsCommand, GetLearningSettingsResponse, GetLearningSettingsUseCase}
import solutions.s4y.vocabla.domain.errors.NotAuthorized
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.identity.IdentifierSchema
import solutions.s4y.vocabla.domain.{User, UserContext}
import solutions.s4y.vocabla.endpoint.http.error.HttpError
import solutions.s4y.vocabla.endpoint.http.error.HttpError.{
  Forbidden403,
  InternalServerError500
}
import solutions.s4y.vocabla.endpoint.http.middleware.BrowserLocale.withLocale
import solutions.s4y.vocabla.endpoint.http.routes.students.prefix
import zio.ZIO
import zio.http.Method.GET
import zio.http.codec.{HttpCodec, PathCodec}
import zio.http.endpoint.{AuthType, Endpoint}
import zio.http.{Response, Route, Status, long}

import java.util.Locale

object GetSettings:

  def endpoint(using
      IdentifierSchema
  ): Endpoint[
    Long,
    GetLearningSettingsCommand,
    HttpError,
    GetLearningSettingsResponse,
    AuthType.Bearer.type
  ] =
    Endpoint(GET / prefix / long("studentId") / "settings")
      .tag(openapiTag)
      .out[GetLearningSettingsResponse]
      .outErrors[HttpError](
        HttpCodec.error[InternalServerError500](Status.InternalServerError),
        HttpCodec.error[Forbidden403](Status.Forbidden)
      )
      .transformIn(id =>
        GetLearningSettingsCommand(id.identifier[User.Student])
      )(command => command.studentId.as[Long])
      .auth(AuthType.Bearer)

  def route(using
      IdentifierSchema
  ): Route[GetLearningSettingsUseCase & Locale & UserContext, Response] =
    endpoint.implement { command =>
      withLocale {
        ZIO.serviceWithZIO[GetLearningSettingsUseCase] { useCase =>
          useCase(command).mapError {
            case e: NotAuthorized => Forbidden403(e.message.localized)
            case e: ServiceFailure =>
              InternalServerError500(e.message.localized)
          }
        }
      }
    }
