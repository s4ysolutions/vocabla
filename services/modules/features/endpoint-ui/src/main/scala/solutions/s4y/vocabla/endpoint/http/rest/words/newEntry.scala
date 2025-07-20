package solutions.s4y.vocabla.endpoint.http.rest.words

import solutions.s4y.vocabla.domain.model.Identifier
import solutions.s4y.vocabla.domain.model.Identifier.IdConverter
import solutions.s4y.vocabla.endpoint.http.rest.error.ErrorResponse.{
  ErrorParseID,
  ErrorService
}
import solutions.s4y.vocabla.words.app.usecase.WordsService
import zio.http.Method.POST
import zio.http.Status
import zio.http.codec.{HttpCodec, HttpContentCodec}
import zio.http.endpoint.Endpoint
import zio.schema.{DeriveSchema, Schema}
import zio.{Tag, ZIO}

import java.util.UUID

private case class NewEntryRequest[OwnerID: IdConverter](
                                                          word: String,
                                                          wordLang: String,
                                                          definition: String,
                                                          definitionLang: String,
                                                          ownerId: Identifier[OwnerID],
                                                          tagLabels: List[String]
)

def [OwnerID: IdConverter]tt(id: Identifier[OwnerID])(using schema: Schema[Identifier[OwnerID]] ) =
  schema.transform(id => id.toId)
  
val x = tt(12.identity[Int])


private given [OwnerID: {IdConverter, Schema}]
    : Schema[NewEntryRequest[OwnerID]] =
  DeriveSchema.gen[NewEntryRequest[OwnerID]]
//private given [OwnerID: IdConverter](using Schema[Identifier[OwnerID]] ): Schema[NewEntryRequest[OwnerID]] = DeriveSchema.gen[NewEntryRequest[OwnerID]]
/*
given [OwnerID](using
    schema: Schema[NewEntryRequest[OwnerID]]
): HttpContentCodec[NewEntryRequest[OwnerID]] =
  HttpContentCodec.fromSchema[NewEntryRequest[OwnerID]]
 */
def newEntryEndpoint[OwnerID: Tag] =
  Endpoint(POST / prefix / "entries")
    .in[NewEntryRequest[OwnerID]]
    .out[NewEntryResponse]
    .outError[ErrorService](Status.InternalServerError)
    .outError[ErrorParseID](Status.BadRequest)

def newEntryRoute[DomainID: Tag, OwnerID: Tag, EntryID: Tag] =
  newEntryEndpoint.implement[WordsService] { request =>
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
    } yield NewEntryResponse(id.toString))
      .mapError(error => Right(ErrorService(error)))
  }
