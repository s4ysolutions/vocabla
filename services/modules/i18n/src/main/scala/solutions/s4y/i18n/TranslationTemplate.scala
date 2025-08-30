package solutions.s4y.i18n

import java.util.Locale

case class TranslationTemplate(
    resolver: TranslationResolver,
    sc: StringContext,
    args: Vector[Any] = Vector.empty
):

  lazy val key: TranslationKey = TranslationKey(sc)

  def apply(newArgs: Any*): TranslationTemplate =
    copy(args = args ++ newArgs.toVector)

  def toString(using locale: Locale): String =
    resolver.resolve(locale, key, args*)

  override def toString: String =
    toString(using Locale.ENGLISH)

object TranslationTemplate:
  extension (tt: TranslationTemplate)
    def render(using locale: Locale): String =
      tt.toString
/*
  given (using Locale): Schema[TranslationTemplate] = Schema[String].transform(
    string => ???,
    template => template.toString
  )
 */
end TranslationTemplate
