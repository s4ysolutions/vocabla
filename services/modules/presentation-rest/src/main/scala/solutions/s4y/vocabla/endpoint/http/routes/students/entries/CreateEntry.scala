package solutions.s4y.vocabla.endpoint.http.routes.students.entries

import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.app.ports.students.entries.entry_create.{
  CreateEntryCommand,
  CreateEntryResponse,
  CreateEntryUseCase
}
import solutions.s4y.vocabla.domain.errors.NotAuthorized
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import solutions.s4y.vocabla.domain.{Entry, Tag, User, UserContext}
import solutions.s4y.vocabla.endpoint.http.error.HttpError
import solutions.s4y.vocabla.endpoint.http.error.HttpError.{
  Forbidden403,
  InternalServerError500
}
import solutions.s4y.vocabla.endpoint.http.middleware.BrowserLocale.withLocale
import solutions.s4y.vocabla.endpoint.http.routes.students.prefix
import zio.http.Method.POST
import zio.http.codec.{HttpCodec, HttpContentCodec}
import zio.http.endpoint.{AuthType, Endpoint}
import zio.http.{Response, Route, Status, long}
import zio.schema.annotation.description
import zio.schema.{Schema, derived}
import zio.{Chunk, ZIO}

import java.util.Locale

object CreateEntry:

  @description("Request to create a new vocabulary entry.")
  final case class CreateEntryRequest(
      @description("The headword of the entry to be created.")
      headword: Entry.Headword,
      @description("The definitions of the entry to be created.")
      definitions: Chunk[Entry.Definition],
      @description("IDs of tags to be associated with the entry.")
      tagIds: Chunk[Identifier[Tag]]
  )

  object CreateEntryRequest:
    given (using IdentifierSchema): Schema[CreateEntryRequest] = Schema.derived

  def endpoint(using
      IdentifierSchema
  ): Endpoint[
    Long,
    CreateEntryCommand,
    HttpError,
    CreateEntryResponse,
    AuthType.Bearer.type
  ] =
    Endpoint(POST / prefix / long("studentId") / "entries")
      .tag(openapiTag)
      .in[CreateEntryRequest]
      .out[CreateEntryResponse]
      .outErrors[HttpError](
        HttpCodec.error[InternalServerError500](Status.InternalServerError),
        HttpCodec.error[Forbidden403](Status.Forbidden)
      )
      .auth(AuthType.Bearer)
      .transformIn((studentId, req) =>
        CreateEntryCommand(
          userId = studentId.identifier[User],
          entry = Entry(
            headword = req.headword,
            definitions = req.definitions,
            ownerId = studentId.identifier[User.Student]
          ),
          tagIds = req.tagIds
        )
      )(cmd =>
        (
          cmd.userId.as[Long],
          CreateEntryRequest(
            headword = cmd.entry.headword,
            definitions = cmd.entry.definitions,
            tagIds = cmd.tagIds
          )
        )
      )

  def route(using
      IdentifierSchema
  ): Route[CreateEntryUseCase & UserContext & Locale, Response] =
    endpoint.implement(command =>
      withLocale {
        ZIO.serviceWithZIO[CreateEntryUseCase](useCase =>
          useCase(command).mapError {
            case e: NotAuthorized => Forbidden403(e.message.localized)
            case e: ServiceFailure =>
              InternalServerError500(e.message.localized)
          }
        )
      }
    )
