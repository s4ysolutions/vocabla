package solutions.s4y.vocabla.domain

import solutions.s4y.vocabla.domain.identity.{
  Identified,
  Identifier,
  IdentifierSchema
}
import zio.prelude.Equal
import zio.schema.{DeriveSchema, Schema}

final case class Tag(
                      label: String,
                      ownerId: Identifier[Student]
):
  override def toString: String = s"Tag: $label"

object Tag:
  given Equal[Tag] =
    Equal.make((a, b) => a.label == b.label)

  given Equal[Identified[Tag]] =
    Equal.make((a, b) => a.e.label == b.e.label)

  given (using is: IdentifierSchema): Schema[Tag] = DeriveSchema.gen[Tag]
