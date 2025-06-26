package solutions.s4y.vocabla.words.domain.model

case class Definition(definition: String, lang: Lang) {
  override def toString: String = s"Definition ($lang) $definition"
}
