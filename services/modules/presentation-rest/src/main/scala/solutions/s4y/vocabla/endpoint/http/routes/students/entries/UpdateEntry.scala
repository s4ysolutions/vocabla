package solutions.s4y.vocabla.endpoint.http.routes.students.entries

import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.app.ports.students.entries.entry_update.{
  UpdateEntryCommand,
  UpdateEntryResponse,
  UpdateEntryUseCase
}
import solutions.s4y.vocabla.domain.errors.NotAuthorized
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import solutions.s4y.vocabla.domain.{Entry, Tag, User, UserContext}
import solutions.s4y.vocabla.endpoint.http.error.HttpError
import solutions.s4y.vocabla.endpoint.http.error.HttpError.{
  BadRequest400,
  Forbidden403,
  InternalServerError500
}
import solutions.s4y.vocabla.endpoint.http.middleware.BrowserLocale.withLocale
import solutions.s4y.vocabla.endpoint.http.routes.students.prefix
import zio.{Chunk, ZIO}
import zio.http.Method.PATCH
import zio.http.codec.HttpCodec
import zio.http.endpoint.{AuthType, Endpoint}
import zio.http.{Response, Route, Status, long}
import zio.schema.annotation.description
import zio.schema.{Schema, derived}

import java.util.Locale

object UpdateEntry:
  // Input payload for update: headword, definitions, tagIds are optional
  final case class UpdateEntryRequest(
      headword: Option[Entry.Headword],
      definitions: Option[Chunk[Entry.Definition]],
      tagIds: Option[Chunk[Identifier[Tag]]]
  )

  object UpdateEntryRequest:
    given (using IdentifierSchema): Schema[UpdateEntryRequest] = Schema.derived

  def endpoint(using
      IdentifierSchema
  ): Endpoint[
    (Long, Long),
    UpdateEntryCommand,
    HttpError,
    UpdateEntryResponse,
    AuthType.Bearer.type
  ] =
    Endpoint(PATCH / prefix / long("studentId") / "entries" / long("entryId"))
      .tag(openapiTag)
      .in[UpdateEntryRequest]
      .out[UpdateEntryResponse]
      .outErrors[HttpError](
        HttpCodec.error[InternalServerError500](Status.InternalServerError),
        HttpCodec.error[Forbidden403](Status.Forbidden),
        HttpCodec.error[BadRequest400](Status.BadRequest)
      )
      .auth(AuthType.Bearer)
      .transformIn { case (userId, entryId, payload) =>
        UpdateEntryCommand(
          userId = userId.identifier[User],
          entryId = entryId.identifier[Entry],
          headword = payload.headword,
          definitions = payload.definitions,
          tagIds = payload.tagIds
        )
      } { command =>
        (
          command.userId.as[Long],
          command.entryId.as[Long],
          UpdateEntryRequest(command.headword, command.definitions, command.tagIds)
        )
      }

  def route(using
      IdentifierSchema
  ): Route[UpdateEntryUseCase & UserContext & Locale, Response] =
    endpoint.implement { command =>
      withLocale {
        ZIO.serviceWithZIO[UpdateEntryUseCase] { useCase =>
          useCase(command).mapError {
            case e: NotAuthorized => Forbidden403(e.message.localized)
            case e: ServiceFailure =>
              InternalServerError500(e.message.localized)
          }
        }
      }
    }
