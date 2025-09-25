package solutions.s4y.vocabla.endpoint.http.routes.students.settings.tags

import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.app.ports.students.ls.tags.{DeleteTagCommand, DeleteTagResponse, DeleteTagUseCase}
import solutions.s4y.vocabla.domain.User.Student
import solutions.s4y.vocabla.domain.errors.NotAuthorized
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.identity.IdentifierSchema
import solutions.s4y.vocabla.domain.{Tag, UserContext}
import solutions.s4y.vocabla.endpoint.http.error.HttpError
import solutions.s4y.vocabla.endpoint.http.error.HttpError.{
  Forbidden403,
  InternalServerError500
}
import solutions.s4y.vocabla.endpoint.http.middleware.BrowserLocale.withLocale
import solutions.s4y.vocabla.endpoint.http.routes.students.prefix
import solutions.s4y.vocabla.endpoint.http.routes.students.settings.openapiTag
import zio.ZIO
import zio.http.Method.DELETE
import zio.http.codec.{HttpCodec, PathCodec}
import zio.http.endpoint.{AuthType, Endpoint}
import zio.http.{Response, Route, Status, long}

import java.util.Locale

object DeleteTag:

  def endpoint(using
      IdentifierSchema
  ): Endpoint[
    (Long, Long),
    DeleteTagCommand,
    HttpError,
    DeleteTagResponse,
    AuthType.Bearer.type
  ] =
    Endpoint(DELETE / prefix / long("studentId") / "settings" / long("tagId"))
      .tag(openapiTag)
      .out[DeleteTagResponse]
      .outErrors[HttpError](
        HttpCodec.error[InternalServerError500](Status.InternalServerError),
        HttpCodec.error[Forbidden403](Status.Forbidden)
      )
      .transformIn((studentId, tagId) =>
        DeleteTagCommand(studentId.identifier[Student], tagId.identifier[Tag])
      )(command => (command.ownerId.as[Long], command.tagId.as[Long]))
      .auth(AuthType.Bearer)

  def route(using
      IdentifierSchema
  ): Route[DeleteTagUseCase & Locale & UserContext, Response] =
    endpoint.implement { command =>
      withLocale {
        ZIO.serviceWithZIO[DeleteTagUseCase] { useCase =>
          useCase(command).mapError {
            case e: NotAuthorized => Forbidden403(e.message.localized)
            case e: ServiceFailure =>
              InternalServerError500(e.message.localized)
          }
        }
      }
    }
