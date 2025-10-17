package solutions.s4y.vocabla.domain

import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import solutions.s4y.vocabla.domain.owner.Owned
import zio.Chunk
import zio.schema.{DeriveSchema, Schema, derived}

final case class Entry(
    headword: Entry.Headword,
    definitions: Chunk[Entry.Definition],
    ownerId: Identifier[User.Student]
) extends Owned[User.Student]:
  override def toString: String = {
    s"Entry: $headword, Definitions: ${definitions.mkString(", ")}"
  }

object Entry:
  given (using is: IdentifierSchema): Schema[Entry] = Schema.derived

  final case class Definition(definition: String, langCode: Lang.Code):
    override def toString: String = s"Definition ($langCode) $definition"

  object Definition:
    given Schema[Definition] = Schema.derived

  final case class Headword(word: String, langCode: Lang.Code):
    override def toString: String = s"Headword ($langCode) $word"

  object Headword:
    given Schema[Headword] = Schema.derived
