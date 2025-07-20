package solutions.s4y.vocabla.endpoint.http.rest.words

import solutions.s4y.vocabla.endpoint.http.codecs.IdCodec
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

given [ID](using codec: IdCodec[ID], tag: Tag[ID]): Schema[ID] =
  Schema[String].transformOrFail(
    (str: String) => codec.string2id(str),
    (id: ID) =>
      Right(id.toString) // Assuming ID has a meaningful toString method
  )

private case class NewEntryRequest[OwnerID](
    word: String,
    wordLang: String,
    definition: String,
    definitionLang: String,
    ownerId: OwnerID,
    tagLabels: List[String]
)

private given [OwnerID: Schema]: Schema[NewEntryRequest[OwnerID]] =
  DeriveSchema.gen[NewEntryRequest[OwnerID]]

private case class NewEntryResponse(entryId: String)

private given Schema[NewEntryResponse] = DeriveSchema.gen[NewEntryResponse]

def newEntryEndpoint[OwnerID: {IdCodec, Tag}] =
  Endpoint(POST / prefix / "entries")
    .in[NewEntryRequest[OwnerID]]
    .out[NewEntryResponse]
    .outError[ErrorService](Status.InternalServerError)
    .outError[ErrorParseID](Status.BadRequest)

def newEntryRoute[DomainID: Tag, OwnerID: {IdCodec, Tag}, EntryID: Tag] =
  newEntryEndpoint.implement[WordsService[DomainID, OwnerID, EntryID]] {
    request =>
      (for {
        wordsService <- ZIO
          .service[WordsService[DomainID, OwnerID, EntryID]]
        id <- wordsService
          .newEntry(
            request.word,
            request.wordLang,
            request.definition,
            request.definitionLang,
            request.ownerId,
            request.tagLabels
          )
      } yield NewEntryResponse(id.toString))
        .mapError(error => Right(ErrorService(error)))
  }
