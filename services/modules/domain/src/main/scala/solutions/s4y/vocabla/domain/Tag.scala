package solutions.s4y.vocabla.domain

import solutions.s4y.vocabla.domain.identity.{
  Identified,
  Identifier,
  IdentifierSchema,
  given
}

import solutions.s4y.vocabla.domain.owner.Owned
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.prelude.Equal
import zio.schema.{Schema, derived}

final case class Tag(
    label: String,
    ownerId: Identifier[User.Student]
) extends Owned[User.Student]:
  override def toString: String = s"Tag: $label"

object Tag:
  given Equal[Tag] =
    Equal.make((a, b) => a.label == b.label)

  given Equal[Identified[Tag]] =
    Equal.make((a, b) => a.e.label == b.e.label)

  given (using is: IdentifierSchema): Schema[Tag] = Schema.derived
  given (using IdentifierSchema): JsonCodec[Tag] = DeriveJsonCodec.gen[Tag]
