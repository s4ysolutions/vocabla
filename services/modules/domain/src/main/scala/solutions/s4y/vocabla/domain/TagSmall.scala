package solutions.s4y.vocabla.domain

import solutions.s4y.vocabla.domain.identity.{
  Identified,
  Identifier,
  IdentifierSchema
}
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.prelude.Equal
import zio.schema.{Schema, derived}

final case class TagSmall(
    label: String
):
  override def toString: String = s"Tag(small): $label"

object TagSmall:
  given Equal[Tag] =
    Equal.make((a, b) => a.label == b.label)

  given Equal[Identified[Tag]] =
    Equal.make((a, b) => a.e.label == b.e.label)

  given (using is: IdentifierSchema): Schema[TagSmall] = Schema.derived
  given (using IdentifierSchema): JsonCodec[TagSmall] =
    DeriveJsonCodec.gen[TagSmall]
