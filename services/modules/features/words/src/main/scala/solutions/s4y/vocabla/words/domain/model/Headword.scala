package solutions.s4y.vocabla.words.domain.model

import solutions.s4y.vocabla.lang.domain.model.Lang

final case class Headword(word: String, langCode: Lang.Code):
  override def toString: String = s"Headword ($langCode) $word"
