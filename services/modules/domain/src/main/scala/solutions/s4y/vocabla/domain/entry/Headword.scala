package solutions.s4y.vocabla.domain.entry

import solutions.s4y.vocabla.domain.Lang

final case class Headword(word: String, langCode: Lang.Code):
  override def toString: String = s"Headword ($langCode) $word"
