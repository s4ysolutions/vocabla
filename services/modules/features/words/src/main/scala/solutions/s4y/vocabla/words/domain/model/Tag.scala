package solutions.s4y.vocabla.words.domain.model

case class Tag(
    id: Tag.Id,
    label: String,
    ownerId: Owner.Id
):
  override def toString: String = s"Tag: $label"

object Tag:
  type Id = Entity.Id
