package solutions.s4y.vocabla.words.domain.model

case class Word[ID](override val id: ID, word: String, lang: Lang)
    extends Model[ID]:
  override def toString: String = s"Word $id: ($lang) $word"
