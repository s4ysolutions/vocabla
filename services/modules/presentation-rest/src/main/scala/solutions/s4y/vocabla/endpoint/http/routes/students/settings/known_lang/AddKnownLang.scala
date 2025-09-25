package solutions.s4y.vocabla.endpoint.http.routes.students.settings.known_lang

import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.app.ports.students.settings.known_lang.{
  AddKnownLangCommand,
  AddKnownLangResponse,
  AddKnownLangUseCase
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
import zio.http.Method.POST
import zio.http.codec.HttpCodec
import zio.http.endpoint.{AuthType, Endpoint}
import zio.schema.annotation.description
import zio.schema.{Schema, derived}

import java.util.Locale

object AddKnownLang:
  @description("Request to add a language the student knows.")
  final case class AddKnownLangRequest(
      @description("Code of the language to be added as known.")
      langCode: Lang.Code
  )

  object AddKnownLangRequest:
    given (using IdentifierSchema): Schema[AddKnownLangRequest] = Schema.derived

  def endpoint(using
      IdentifierSchema
  ): Endpoint[
    Long,
    AddKnownLangCommand,
    HttpError,
    AddKnownLangResponse,
    AuthType.Bearer.type
  ] = Endpoint(POST / prefix / long("studentId") / "settings" / "known-languages")
    .tag(openapiTag)
    .in[AddKnownLangRequest]
    .out[AddKnownLangResponse]
    .outErrors[HttpError](
      HttpCodec.error[InternalServerError500](Status.InternalServerError),
      HttpCodec.error[Forbidden403](Status.Forbidden)
    )
    .auth(AuthType.Bearer)
    .transformIn((studentId, request) =>
      AddKnownLangCommand(
        langCode = request.langCode,
        studentId = studentId.identifier[User.Student]
      )
    )(command =>
      (command.studentId.as[Long], AddKnownLangRequest(command.langCode))
    )

  def route(using
      IdentifierSchema
  ): Route[AddKnownLangUseCase & Locale & UserContext, Response] =
    endpoint.implement { command =>
      withLocale {
        ZIO.serviceWithZIO[AddKnownLangUseCase](useCase =>
          useCase(command).mapError {
            case e: NotAuthorized => Forbidden403(e.message.localized)
            case e: ServiceFailure =>
              InternalServerError500(e.message.localized)
          }
        )
      }
    }
