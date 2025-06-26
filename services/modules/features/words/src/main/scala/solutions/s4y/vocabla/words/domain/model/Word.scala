package solutions.s4y.vocabla.words.domain.model

case class Word(id: Word.Id, word: String, lang: Lang):
  override def toString: String = s"Word $id: ($lang) $word"

object Word:
  type Id = Entity.Id
