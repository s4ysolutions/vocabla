package solutions.s4y.vocabla.endpoint.http.routes.students.settings.known_lang

import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.app.ports.students.settings.known_lang.{
  RemoveKnownLangCommand,
  RemoveKnownLangResponse,
  RemoveKnownLangUseCase
}
import solutions.s4y.vocabla.domain.errors.NotAuthorized
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.identity.IdentifierSchema
import solutions.s4y.vocabla.domain.{Lang, User, UserContext}
import solutions.s4y.vocabla.endpoint.http.error.HttpError
import solutions.s4y.vocabla.endpoint.http.error.HttpError.{
  Forbidden403,
  InternalServerError500
}
import solutions.s4y.vocabla.endpoint.http.middleware.BrowserLocale.withLocale
import solutions.s4y.vocabla.endpoint.http.routes.students.prefix
import solutions.s4y.vocabla.endpoint.http.routes.students.settings.openapiTag
import zio.ZIO
import zio.http.*
import zio.http.Method.DELETE
import zio.http.codec.HttpCodec
import zio.http.endpoint.{AuthType, Endpoint}

import java.util.Locale

object RemoveKnownLang:
  def endpoint(using
      IdentifierSchema
  ): Endpoint[
    (Long, String),
    RemoveKnownLangCommand,
    HttpError,
    RemoveKnownLangResponse,
    AuthType.Bearer.type
  ] = Endpoint(
    DELETE / prefix / long(
      "studentId"
    ) / "settings" / "known-languages" / string("langCode")
  )
    .tag(openapiTag)
    .out[RemoveKnownLangResponse]
    .outErrors[HttpError](
      HttpCodec.error[InternalServerError500](Status.InternalServerError),
      HttpCodec.error[Forbidden403](Status.Forbidden)
    )
    .auth(AuthType.Bearer)
    .transformIn((studentId, langCode) =>
      RemoveKnownLangCommand(
        langCode = Lang.Code(langCode),
        studentId = studentId.identifier[User.Student]
      )
    )(command => (command.studentId.as[Long], command.langCode))

  def route(using
      IdentifierSchema
  ): Route[RemoveKnownLangUseCase & Locale & UserContext, Response] =
    endpoint.implement { command =>
      withLocale {
        ZIO.serviceWithZIO[RemoveKnownLangUseCase](useCase =>
          useCase(command).mapError {
            case e: NotAuthorized => Forbidden403(e.message.localized)
            case e: ServiceFailure =>
              InternalServerError500(e.message.localized)
          }
        )
      }
    }
