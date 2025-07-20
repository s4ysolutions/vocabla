package solutions.s4y.vocabla.words.domain.model

import solutions.s4y.vocabla.lang.domain.model.Lang

case class Headword(word: String, lang: Lang):
  override def toString: String = s"Headword ($lang) $word"
