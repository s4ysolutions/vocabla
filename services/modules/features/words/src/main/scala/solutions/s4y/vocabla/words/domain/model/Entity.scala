package solutions.s4y.vocabla.words.domain.model

object Entity:
  opaque type Id = Long
  def Id(value: Long): Id = value
  extension (id: Id) def long: Long = id
  given Conversion[Long, Entity.Id] = Id(_)
