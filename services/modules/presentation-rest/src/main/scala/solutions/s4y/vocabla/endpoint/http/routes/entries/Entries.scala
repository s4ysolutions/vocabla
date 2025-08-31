package solutions.s4y.vocabla.endpoint.http.routes.entries




/*
import solutions.s4y.vocabla.domain.Entry
import solutions.s4y.vocabla.domain.identity.{Identified, IdentifierSchema}
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.endpoint.http.rest.error.ErrorResponse.{ErrorParseID, ErrorService}
import solutions.s4y.vocabla.entries.app.ports.EntryService
import solutions.s4y.vocabla.entries.domain.model.Owner
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
    uuid("ownerId").transform(
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
    Endpoint(GET / prefix / "entries" / uuid("ownerId"))
      // .in[EntriesRequest]
      .out[EntriesResponse]
      .outError[ErrorService](Status.InternalServerError)
      .outError[ErrorParseID](Status.BadRequest)

  def route(using IdentifierSchema): Route[EntryService, Nothing] =
    endpoint.implement[EntryService] { ownerId =>
      (for {
        wordsService <- ZIO.service[EntryService]
        entries <- wordsService.getEntriesForOwner(
          ownerId.identifier[Owner]
        ) // Identifier[Owner](request.path("ownerId"))
      } yield EntriesResponse(entries))
        .mapError(error => Right(ErrorService(error)))
    }

  private given (using IdentifierSchema): Schema[EntriesResponse] =
    DeriveSchema.gen[EntriesResponse]

val entries = Seq(
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
  "create",
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
