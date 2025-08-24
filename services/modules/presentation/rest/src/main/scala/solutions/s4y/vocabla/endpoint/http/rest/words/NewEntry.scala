package solutions.s4y.vocabla.endpoint.http.rest.words
/*
import solutions.s4y.vocabla.app.ports.{CreateEntryCommand, CreateEntryUseCase}
import solutions.s4y.vocabla.domain.Entry
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import solutions.s4y.vocabla.endpoint.http.rest.error.ErrorResponse
import solutions.s4y.vocabla.endpoint.http.rest.error.ErrorResponse.{
  ErrorParseID,
  ErrorService
}
import zio.http.Method.POST
import zio.http.codec.HttpContentCodec
import zio.http.endpoint.AuthType.None
import zio.http.endpoint.Endpoint
import zio.http.{Route, Status}
import zio.schema.{DeriveSchema, Schema}
import zio.{Chunk, ZIO}

case class NewEntryRequest(entry: Entry)
case class NewEntryResponse(entryId: Identifier[Entry])

object NewEntry:

  def endpoint(using
      IdentifierSchema
  ): Endpoint[Unit, NewEntryRequest, Either[
    ErrorResponse.ErrorParseID,
    ErrorResponse.ErrorService
  ], NewEntryResponse, None] =
    Endpoint(POST / prefix / "entries")
      .in[NewEntryRequest]
      .out[NewEntryResponse]
      .outError[ErrorService](Status.InternalServerError)
      .outError[ErrorParseID](Status.BadRequest)

  def route(using IdentifierSchema): Route[CreateEntryUseCase, Nothing] =
    endpoint.implement { request =>
      ZIO
        .serviceWithZIO[CreateEntryUseCase] {
          _.apply(CreateEntryCommand(request.entry, Chunk.empty, 1L.identifier))
        }
        .mapBoth(
          error => Right(ErrorService(error)),
          id => NewEntryResponse(id)
        )
    }

  private given (using IdentifierSchema): Schema[NewEntryRequest] =
    DeriveSchema.gen[NewEntryRequest]

  private given (using IdentifierSchema): Schema[NewEntryResponse] =
    DeriveSchema.gen[NewEntryResponse]
 */
