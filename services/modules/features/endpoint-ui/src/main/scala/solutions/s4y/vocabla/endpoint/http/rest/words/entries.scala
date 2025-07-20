package solutions.s4y.vocabla.endpoint.http.rest.words

import solutions.s4y.vocabla.endpoint.http.rest.error.ErrorResponse.{
  ErrorParseID,
  ErrorService
}
import solutions.s4y.vocabla.endpoint.http.schema.given
import solutions.s4y.vocabla.words.app.repo.dto.{DefinitionDTO, EntryDTO, given}
import zio.ZIO
import zio.http.Method.GET
import zio.http.endpoint.Endpoint
import zio.http.{Status, string}
import zio.schema.Schema.list
import zio.schema.{DeriveSchema, Schema}

private case class EntriesResponse(
    entries: List[EntryDTO[String, String]]
)

private given Schema[EntriesResponse] = DeriveSchema.gen[EntriesResponse]

val entriesEndpoint = Endpoint(GET / prefix / "entries" / string("ownerId"))
  // .in[EntriesRequest]
  .out[EntriesResponse]
  .outError[ErrorService](Status.InternalServerError)
  .outError[ErrorParseID](Status.BadRequest)

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

val entriesRoute =
  entriesEndpoint.implement { request =>
    val entries = words.map[EntryDTO[String, String]](w =>
      EntryDTO(
        id = System.nanoTime().toString,
        word = w,
        lang = "en",
        definitions = List(DefinitionDTO(s"definition of $w", "ne")),
        tags = List("tag1", "tag2")
      )
    )
    ZIO.succeed(EntriesResponse(entries.toList))
  }
