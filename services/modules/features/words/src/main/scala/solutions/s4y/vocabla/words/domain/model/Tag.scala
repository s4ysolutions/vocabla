package solutions.s4y.vocabla.words.domain.model

import solutions.s4y.vocabla.domain.model.Identifier.*
import solutions.s4y.vocabla.domain.model.{Identifier, IdentifierSchema}
import zio.prelude.Equal
import zio.schema.{DeriveSchema, Schema}

case class Tag(
    label: String,
    owner: Identifier[Owner]
):
  override def toString: String = s"Tag: $label"

object Tag:
  def apply[ID](label: String, ownerId: ID) =
    new Tag(label, ownerId.identifier[Owner])

  given equalTag: Equal[Tag] =
    Equal.make((a, b) => a.label == b.label && a.owner == b.owner)

  given (using is: IdentifierSchema): Schema[Tag] = DeriveSchema.gen[Tag]
