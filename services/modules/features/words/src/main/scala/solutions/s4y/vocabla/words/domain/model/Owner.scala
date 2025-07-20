package solutions.s4y.vocabla.words.domain.model

case class Owner(
    name: String
) {
  override def toString: String = s"Owner($name)"
}
