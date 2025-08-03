package solutions.s4y.vocabla.endpoint.http.rest.words

import solutions.s4y.vocabla.domain.model.{Identifier, IdentifierSchema}
import solutions.s4y.vocabla.endpoint.http.rest.error.ErrorResponse
import solutions.s4y.vocabla.endpoint.http.rest.error.ErrorResponse.{
  ErrorParseID,
  ErrorService
}
import solutions.s4y.vocabla.words.app.ports.EntryService
import solutions.s4y.vocabla.words.domain.model.Entry
import zio.ZIO
import zio.http.Method.POST
import zio.http.codec.HttpContentCodec
import zio.http.endpoint.AuthType.None
import zio.http.endpoint.Endpoint
import zio.http.{Route, Status}
import zio.schema.{DeriveSchema, Schema}

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

  def route(using IdentifierSchema): Route[EntryService, Nothing] =
    endpoint.implement[EntryService] { request =>
      ZIO
        .serviceWithZIO[EntryService] { _.newEntry(request.entry) }
        .mapBoth(
          error => Right(ErrorService(error)),
          id => NewEntryResponse(id)
        )
    }

  private given (using IdentifierSchema): Schema[NewEntryRequest] =
    DeriveSchema.gen[NewEntryRequest]

  private given (using IdentifierSchema): Schema[NewEntryResponse] =
    DeriveSchema.gen[NewEntryResponse]
