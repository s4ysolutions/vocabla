package solutions.s4y.vocabla.domain

final case class Lang(code: Lang.Code, flag: String, name: String) {
  override def toString: String = s"Lang: $code $flag $name"
}

object Lang:
  type Code = String
  
  given Conversion[String, Code] with
    def apply(code: String): Code = code
