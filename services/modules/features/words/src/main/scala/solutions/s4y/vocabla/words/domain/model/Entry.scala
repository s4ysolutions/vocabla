package solutions.s4y.vocabla.words.domain.model

import solutions.s4y.vocabla.domain.model.Identifier.given_Schema_Seq
import solutions.s4y.vocabla.domain.model.{Identifier, IdentifierSchema}
import zio.Chunk
import zio.schema.{DeriveSchema, Schema}

case class Entry(
    headword: Headword,
    definitions: Chunk[Definition],
    tags: Chunk[Identifier[Tag]],
    ownerId: Identifier[Owner]
):
  override def toString: String =
    s"Entry: $headword, Definitions: ${definitions.mkString(", ")}, Tags: ${tags.mkString(", ")}"

object Entry:
  given (using is: IdentifierSchema): Schema[Entry] = DeriveSchema.gen[Entry]
