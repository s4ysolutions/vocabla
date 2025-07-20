package solutions.s4y.vocabla.words.domain.model

import solutions.s4y.vocabla.domain.model.Identifier
import solutions.s4y.vocabla.id.IdFactory
import zio.UIO
import zio.prelude.Equal

case class Tag(
    label: String,
    ownerId: Identifier[Owner]
):
  override def toString: String = s"Tag: $label"

object Tag:
  given equalTag: Equal[Tag] =
    Equal.make((a, b) => a.label == b.label && a.ownerId == b.ownerId)
