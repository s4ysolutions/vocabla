package solutions.s4y.vocabla.words.domain.model

import solutions.s4y.vocabla.domain.model.{Identifier, IdentifierSchema}
import solutions.s4y.vocabla.tags.domain.Tag
import zio.Chunk
import zio.schema.{DeriveSchema, Schema}

final case class Entry(
    headword: Headword,
    definitions: Chunk[Definition]
):
  override def toString: String = {
    s"Entry: $headword, Definitions: ${definitions.mkString(", ")}"
  }

object Entry:
  given (using is: IdentifierSchema): Schema[Entry] = DeriveSchema.gen[Entry]
