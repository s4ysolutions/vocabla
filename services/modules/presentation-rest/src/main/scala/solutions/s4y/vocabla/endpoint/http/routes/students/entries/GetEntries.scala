package solutions.s4y.vocabla.endpoint.http.routes.students.entries

import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.app.ports.students.entries.entries_get.{
  GetEntriesCommand,
  GetEntriesResponse,
  GetEntriesUseCase
}
import solutions.s4y.vocabla.domain.errors.NotAuthorized
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import solutions.s4y.vocabla.domain.{Lang, Tag, User, UserContext}
import solutions.s4y.vocabla.endpoint.http.error.HttpError
import solutions.s4y.vocabla.endpoint.http.error.HttpError.{
  BadRequest400,
  Forbidden403,
  InternalServerError500
}
import solutions.s4y.vocabla.endpoint.http.middleware.BrowserLocale.withLocale
import solutions.s4y.vocabla.endpoint.http.routes.students.prefix
import zio.http.Method.GET
import zio.http.codec.HttpCodec
import zio.http.endpoint.AuthType.None
import zio.http.endpoint.Endpoint
import zio.http.{Response, Route, Status, long}
import zio.schema.annotation.description
import zio.{Chunk, ZIO}

import java.util.Locale

object GetEntries:
  @description("Request to get entries with optional filters.")
  final case class GetEntriesRequest(
      @description("ID of the owner to filter entries by.")
      ownerId: Option[Identifier[User]],
      @description("Tag IDs to filter entries by.")
      tagId: Chunk[Identifier[Tag]],
      @description("Languages to filter entries by.")
      lang: Chunk[Lang.Code],
      @description("Text to search for in entries.")
      text: Option[String]
  )

  object GetEntriesRequest:
    given (using IdentifierSchema): zio.schema.Schema[GetEntriesRequest] =
      zio.schema.DeriveSchema.gen[GetEntriesRequest]

  def endpoint(using
      IdentifierSchema
  ): Endpoint[
    Long,
    GetEntriesCommand,
    HttpError,
    GetEntriesResponse,
    None
  ] =
    Endpoint(GET / prefix / long("studentId") / "entries")
      .tag(openapiTag)
      .query(
        HttpCodec
          .query[GetEntriesRequest]
      )
      .out[GetEntriesResponse]
      .outErrors[HttpError](
        HttpCodec.error[BadRequest400](Status.BadRequest),
        HttpCodec.error[Forbidden403](Status.Forbidden),
        HttpCodec.error[InternalServerError500](Status.InternalServerError)
      )
      .transformIn((studentId, req) =>
        GetEntriesCommand(
          userId = studentId.identifier[User],
          ownerId = req.ownerId,
          tagIds = req.tagId,
          langs = req.lang,
          text = req.text
        )
      )(command =>
        (
          command.userId.as[Long],
          GetEntriesRequest(
            ownerId = command.ownerId,
            tagId = command.tagIds,
            lang = command.langs,
            text = command.text
          )
        )
      )

  def route(using
      IdentifierSchema
  ): Route[GetEntriesUseCase & Locale & UserContext, Response] =
    endpoint.implement { command =>
      withLocale {
        ZIO.serviceWithZIO[GetEntriesUseCase] { useCase =>
          useCase(command).mapError {
            case e: NotAuthorized => Forbidden403(e.message.localized)
            case e: ServiceFailure =>
              InternalServerError500(e.message.localized)
          }
        }
      }
    }

end GetEntries
