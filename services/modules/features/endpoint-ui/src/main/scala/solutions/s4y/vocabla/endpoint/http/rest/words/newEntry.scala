package solutions.s4y.vocabla.endpoint.http.rest.words

import solutions.s4y.vocabla.endpoint.http.codecs.IdCodec
import solutions.s4y.vocabla.endpoint.http.codecs.IdCodec.toID
import solutions.s4y.vocabla.endpoint.http.rest.error.ErrorResponse.{
  ErrorParseID,
  ErrorService
}
import solutions.s4y.vocabla.words.app.usecase.WordsService
import zio.http.Method.POST
import zio.http.Status
import zio.http.endpoint.Endpoint
import zio.schema.{DeriveSchema, Schema}
import zio.{Tag, ZIO}

private case class NewEntryRequest(
    word: String,
    wordLang: String,
    definition: String,
    definitionLang: String,
    ownerId: String,
    tagLabels: List[String]
)

private given Schema[NewEntryRequest] = DeriveSchema.gen[NewEntryRequest]

private case class NewEntryResponse(entryId: String)

private given Schema[NewEntryResponse] = DeriveSchema.gen[NewEntryResponse]

val newEntryEndpoint = Endpoint(POST / prefix / "entries")
  .in[NewEntryRequest]
  .out[NewEntryResponse]
  .outError[ErrorService](Status.InternalServerError)
  .outError[ErrorParseID](Status.BadRequest)

def newEntryRoute[DomainID: Tag, OwnerID: {IdCodec, Tag}, EntryID: Tag] =
  newEntryEndpoint.implement[WordsService[DomainID, OwnerID, EntryID]] {
    request =>
      request.ownerId
        .toID[OwnerID]
        .fold(
          error => ZIO.fail(Left(ErrorParseID(error))),
          ownerId =>
            (for {
              wordsService <- ZIO
                .service[WordsService[DomainID, OwnerID, EntryID]]
              id <- wordsService
                .newEntry(
                  request.word,
                  request.wordLang,
                  request.definition,
                  request.definitionLang,
                  ownerId,
                  request.tagLabels
                )
            } yield NewEntryResponse(id.toString))
              .mapError(error => Right(ErrorService(error)))
        )
  }
