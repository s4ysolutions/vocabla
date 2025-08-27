package solutions.s4y.i18n

import java.util.Locale

case class TranslationTemplate(
    sc: StringContext,
    args: Vector[Any] = Vector.empty
):

  lazy val key: TranslationKey = TranslationKey(sc.parts.mkString("{}"))

  def apply(newArgs: Any*): TranslationTemplate =
    copy(args = args ++ newArgs.toVector)

  def toString(using locale: Locale, resolver: TranslationResolver): String =
    resolver.resolve(key, args.toArray, locale)

  def toString(resolver: TranslationResolver): String = {
    val locale = Locale.getDefault
    toString(using locale, resolver)
  }

extension (tt: TranslationTemplate)
  def render(using locale: Locale, resolver: TranslationResolver): String =
    tt.toString
