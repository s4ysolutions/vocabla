package solutions.s4y.vocabla.words.domain.model

import solutions.s4y.vocabla.lang.domain.model.Lang

case class Definition(definition: String, lang: Lang) {
  override def toString: String = s"Definition ($lang) $definition"
}
