package solutions.s4y.vocabla.endpoint.http.routes.students.settings.learn_lang

import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.app.ports.students.settings.learn_lang.{
  AddLearnLangCommand,
  AddLearnLangResponse,
  AddLearnLangUseCase
}
import solutions.s4y.vocabla.domain.errors.{InvalidLangCode, NotAuthorized}
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.identity.IdentifierSchema
import solutions.s4y.vocabla.domain.{User, UserContext}
import solutions.s4y.vocabla.endpoint.http.error.HttpError
import solutions.s4y.vocabla.endpoint.http.error.HttpError.{
  Forbidden403,
  InternalServerError500,
  UnprocessableEntity422
}
import solutions.s4y.vocabla.endpoint.http.middleware.BrowserLocale.withLocale
import solutions.s4y.vocabla.endpoint.http.routes.students.prefix
import solutions.s4y.vocabla.endpoint.http.routes.students.settings.openapiTag
import zio.http.*
import zio.http.Method.POST
import zio.http.codec.HttpCodec
import zio.http.endpoint.{AuthType, Endpoint}
import zio.{NonEmptyChunk, ZIO}

import java.util.Locale

object AddLearnLang:
  def endpoint(using
      IdentifierSchema
  ): Endpoint[
    (Long, String),
    AddLearnLangCommand,
    HttpError,
    AddLearnLangResponse,
    AuthType.Bearer.type
  ] = Endpoint(
    POST / prefix / long(
      "studentId"
    ) / "learning-settings" / "learn-languages" / string("langCode")
  )
    .tag(openapiTag)
    .out[AddLearnLangResponse]
    .outErrors[HttpError](
      HttpCodec.error[InternalServerError500](Status.InternalServerError),
      HttpCodec.error[Forbidden403](Status.Forbidden),
      HttpCodec.error[UnprocessableEntity422](Status.UnprocessableEntity)
    )
    .auth(AuthType.Bearer)
    .transformIn((studentId, langCode) =>
      AddLearnLangCommand(
        langCode = langCode,
        studentId = studentId.identifier[User.Student]
      )
    )(command => (command.studentId.as[Long], command.langCode))

  def route(using
      IdentifierSchema
  ): Route[AddLearnLangUseCase & Locale & UserContext, Response] =
    endpoint.implement { command =>
      withLocale {
        ZIO.serviceWithZIO[AddLearnLangUseCase](useCase =>
          useCase(command).mapError {
            case e: NotAuthorized => Forbidden403(e.message.localized)
            case e: ServiceFailure =>
              InternalServerError500(e.message.localized)
            case e: InvalidLangCode =>
              UnprocessableEntity422(NonEmptyChunk(e.message.localized))
          }
        )
      }
    }
