package solutions.s4y.vocabla.domain

import solutions.s4y.vocabla.domain.entry.{Definition, Headword}
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import zio.Chunk
import zio.schema.{DeriveSchema, Schema}

final case class Entry(
    headword: Headword,
    definitions: Chunk[Definition],
    ownerId: Identifier[Student]
):
  override def toString: String = {
    s"Entry: $headword, Definitions: ${definitions.mkString(", ")}"
  }

object Entry:
  given (using is: IdentifierSchema): Schema[Entry] = DeriveSchema.gen[Entry]
