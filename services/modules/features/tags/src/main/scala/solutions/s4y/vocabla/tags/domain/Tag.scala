package solutions.s4y.vocabla.tags.domain

import solutions.s4y.vocabla.domain.model.Identifier.*
import solutions.s4y.vocabla.domain.model.{
  Identified,
  Identifier,
  IdentifierSchema
}
import zio.prelude.Equal
import zio.schema.{DeriveSchema, Schema}

final case class Tag(
    label: String
):
  override def toString: String = s"Tag: $label"

object Tag:
  given equalTag: Equal[Tag] =
    Equal.make((a, b) => a.label == b.label)

  given equalIdentifiedTag: Equal[Identified[Tag]] =
    Equal.make((a, b) => a.e.label == b.e.label)

  given (using is: IdentifierSchema): Schema[Tag] = DeriveSchema.gen[Tag]
