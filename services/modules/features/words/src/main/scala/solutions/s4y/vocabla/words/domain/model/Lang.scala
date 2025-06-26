package solutions.s4y.vocabla.words.domain.model

case class Lang(code: Lang.Code, flag: String, name: String) {
  override def toString: String = s"Lang: $code $flag $name"
}

object Lang:
  type Code = String
