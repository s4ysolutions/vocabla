package solutions.s4y.i18n

opaque type TranslationKey = String

object TranslationKey:
  def apply(key: String): TranslationKey = key
  def apply(sc: StringContext): TranslationKey = sc.parts.mkString("{}")
  final val re = "\\{\\}".r
  extension (tk: TranslationKey)
    def value: String = {
      var counter = 0
      re.replaceAllIn(
        tk,
        _ =>
          s"{${counter}}" match {
            case result => counter += 1; result
          }
      )
    }
