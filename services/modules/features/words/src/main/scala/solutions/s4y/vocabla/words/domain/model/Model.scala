package solutions.s4y.vocabla.words.domain.model

/** On the domain level all IDs are of the same type
  * @tparam ID
  *   type used to represent IDs of all domain models
  */
trait Model[ID]:
  val id: ID
