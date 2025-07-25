package solutions.s4y.vocabla.endpoint.http.rest.words

import solutions.s4y.vocabla.domain.model.Identifier.identifier
import solutions.s4y.vocabla.domain.model.{Identified, IdentifierSchema}
import solutions.s4y.vocabla.endpoint.http.rest.error.ErrorResponse.{
  ErrorParseID,
  ErrorService
}
import solutions.s4y.vocabla.words.app.usecase.WordsService
import solutions.s4y.vocabla.words.domain.model.{Entry, Owner}
import zio.http.Method.GET
import zio.http.codec.PathCodec
import zio.http.endpoint.AuthType.None
import zio.http.endpoint.Endpoint
import zio.http.{Route, Status, uuid}
import zio.schema.{DeriveSchema, Schema}
import zio.{Chunk, ZIO}

import java.util.UUID

case class EntriesResponse(
    entries: Chunk[Identified[Entry]]
)

object Entries:
  /* TODO:
  private def ownerIdCodec(using
      IdentifierSchema
  ): PathCodec[Identifier[Owner]] =
    uuid("owner").transform(
      (_uuid: UUID) => _uuid.identifier[Owner],
      (id: Identifier[Owner]) => id.as[UUID]
    )
   */
  def endpoint(using
      IdentifierSchema
  ): Endpoint[UUID, UUID, Either[
    ErrorParseID,
    ErrorService
  ], EntriesResponse, None] =
    Endpoint(GET / prefix / "entries" / uuid("owner"))
      // .in[EntriesRequest]
      .out[EntriesResponse]
      .outError[ErrorService](Status.InternalServerError)
      .outError[ErrorParseID](Status.BadRequest)

  def route(using IdentifierSchema): Route[WordsService, Nothing] =
    endpoint.implement[WordsService] { ownerId =>
      (for {
        wordsService <- ZIO.service[WordsService]
        entries <- wordsService.getEntriesForOwner(
          ownerId.identifier[Owner]
        ) // Identifier[Owner](request.path("owner"))
      } yield EntriesResponse(entries))
        .mapError(error => Right(ErrorService(error)))
    }

  private given (using IdentifierSchema): Schema[EntriesResponse] =
    DeriveSchema.gen[EntriesResponse]
/*
val words = Seq(
  "abandon",
  "ability",
  "able",
  "about",
  "above",
  "accept",
  "access",
  "accident",
  "account",
  "achieve",
  "acquire",
  "across",
  "action",
  "active",
  "activity",
  "actor",
  "actual",
  "adapt",
  "add",
  "address",
  "adjust",
  "admit",
  "adult",
  "advance",
  "advice",
  "affect",
  "afford",
  "after",
  "again",
  "against",
  "agency",
  "agent",
  "agree",
  "ahead",
  "allow",
  "almost",
  "alone",
  "along",
  "already",
  "also",
  "although",
  "always",
  "amaze",
  "among",
  "amount",
  "analysis",
  "ancient",
  "anger",
  "animal",
  "announce",
  "annual",
  "another",
  "answer",
  "anxiety",
  "anyone",
  "anything",
  "anyway",
  "apart",
  "apology",
  "appeal",
  "appear",
  "apply",
  "appoint",
  "approach",
  "approve",
  "argue",
  "arise"
);
 */
