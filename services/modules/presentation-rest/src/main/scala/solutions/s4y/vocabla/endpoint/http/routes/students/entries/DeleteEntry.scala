package solutions.s4y.vocabla.endpoint.http.routes.students.entries

import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.app.ports.students.entries.entry_delete.{
  DeleteEntryCommand,
  DeleteEntryResponse,
  DeleteEntryUseCase
}
import solutions.s4y.vocabla.domain.errors.NotAuthorized
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import solutions.s4y.vocabla.domain.{Entry, User, UserContext}
import solutions.s4y.vocabla.endpoint.http.error.HttpError
import solutions.s4y.vocabla.endpoint.http.error.HttpError.{
  Forbidden403,
  InternalServerError500
}
import solutions.s4y.vocabla.endpoint.http.middleware.BrowserLocale.withLocale
import solutions.s4y.vocabla.endpoint.http.routes.students.prefix
import zio.ZIO
import zio.http.Method.DELETE
import zio.http.codec.HttpCodec
import zio.http.endpoint.AuthType.None
import zio.http.endpoint.Endpoint
import zio.http.{Response, Route, Status, long}

import java.util.Locale

object DeleteEntry:
  def endpoint(using
      IdentifierSchema
  ): Endpoint[
    (Long, Long),
    DeleteEntryCommand,
    HttpError,
    DeleteEntryResponse,
    None
  ] =
    Endpoint(DELETE / prefix / long("studentId") / "entries" / long("entryId"))
      .tag(openapiTag)
      .out[DeleteEntryResponse]
      .outErrors[HttpError](
        HttpCodec.error[InternalServerError500](Status.InternalServerError),
        HttpCodec.error[Forbidden403](Status.Forbidden)
      )
      .transformIn((userId, entryId) =>
        DeleteEntryCommand(
          userId = userId.identifier[User],
          entryId = entryId.identifier[Entry]
        )
      )(command => (command.userId.as[Long], command.entryId.as[Long]))

  def route(using
      IdentifierSchema
  ): Route[DeleteEntryUseCase & Locale & UserContext, Response] =
    endpoint.implement { command =>
      withLocale {
        ZIO.serviceWithZIO[DeleteEntryUseCase] { useCase =>
          useCase(command).mapError {
            case e: NotAuthorized => Forbidden403(e.message.localized)
            case e: ServiceFailure =>
              InternalServerError500(e.message.localized)
          }
        }
      }
    }

