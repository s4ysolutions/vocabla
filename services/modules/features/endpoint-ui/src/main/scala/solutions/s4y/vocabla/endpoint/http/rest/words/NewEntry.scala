package solutions.s4y.vocabla.endpoint.http.rest.words

import solutions.s4y.vocabla.domain.model.{Identifier, IdentifierSchema}
import solutions.s4y.vocabla.endpoint.http.rest.error.ErrorResponse
import solutions.s4y.vocabla.endpoint.http.rest.error.ErrorResponse.{
  ErrorParseID,
  ErrorService
}
import solutions.s4y.vocabla.words.app.usecase.WordsService
import solutions.s4y.vocabla.words.domain.model.{Entry, Owner}
import zio.http.Method.POST
import zio.http.codec.HttpContentCodec
import zio.http.endpoint.AuthType.None
import zio.http.endpoint.Endpoint
import zio.http.{Route, Status}
import zio.schema.{DeriveSchema, Schema}
import zio.{Chunk, ZIO}

case class NewEntryRequest(
                       word: String,
                       wordLang: String,
                       definition: String,
                       definitionLang: String,
                       ownerId: Identifier[Owner],
                       tagLabels: Chunk[String]
                     )
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

  def route(using IdentifierSchema): Route[WordsService, Nothing] =
    endpoint.implement[WordsService] { request =>
      (for {
        wordsService <- ZIO
          .service[WordsService]
        id <- wordsService
          .newEntry(
            request.word,
            request.wordLang,
            request.definition,
            request.definitionLang,
            request.ownerId,
            request.tagLabels
          )
      } yield NewEntryResponse(id))
        .mapError(error => Right(ErrorService(error)))
    }

  private given (using IdentifierSchema): Schema[NewEntryRequest] =
    DeriveSchema.gen[NewEntryRequest]

  private given (using IdentifierSchema): Schema[NewEntryResponse] =
    DeriveSchema.gen[NewEntryResponse]
