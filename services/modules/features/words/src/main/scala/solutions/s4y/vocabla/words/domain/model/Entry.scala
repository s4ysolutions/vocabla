package solutions.s4y.vocabla.words.domain.model

import solutions.s4y.vocabla.domain.model.Identifier.identifier
import solutions.s4y.vocabla.domain.model.{Identifier, IdentifierSchema}
import zio.Chunk
import zio.schema.{DeriveSchema, Schema}

final case class Entry(
    headword: Headword,
    definitions: Chunk[Definition],
    tags: Chunk[Identifier[Tag]],
    owner: Identifier[Owner]
):
  override def toString: String =
    s"Entry: $headword, Definitions: ${definitions.mkString(", ")}, Tags: ${tags.mkString(", ")}"

object Entry:
  def apply[OwnerID](
      headword: Headword,
      definitions: Chunk[Definition],
      tags: Chunk[Identifier[Tag]],
      owner: OwnerID
  ): Entry = new Entry(headword, definitions, tags, owner.identifier[Owner])
  given (using is: IdentifierSchema): Schema[Entry] = DeriveSchema.gen[Entry]
