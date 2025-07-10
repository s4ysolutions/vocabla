package solutions.s4y.vocabla.words.domain.model

import solutions.s4y.vocabla.id.IdFactory
import zio.UIO

case class Tag[ID](
    override val id: ID,
    label: String,
    ownerId: ID
) extends Model[ID]:
  override def toString: String = s"Tag: $label"
/*
object Tag:
  type Id = Model.Id
  def Id(value: Model.Id): Id = Entry.Id(value)

  given IdFactory[Id] = summon[IdFactory[Model.Id]].map(id => Id(id))
 */
