package solutions.s4y.i18n

import java.text.MessageFormat
import java.util.{Locale, ResourceBundle}

class ResourcesBundleResolver(baseName: String) extends TranslationResolver:
  private val bundles = scala.collection.mutable.Map[Locale, ResourceBundle]()

  private def getBundle(locale: Locale): ResourceBundle =
    bundles.getOrElseUpdate(locale, ResourceBundle.getBundle(baseName, locale))

  override def resolve(
      locale: Locale,
      key: TranslationKey,
      args: Any*
  ): String =
    val bundle = getBundle(locale)
    val pattern = bundle.getString(key.value)
    if args.nonEmpty then MessageFormat.format(pattern, args*)
    else pattern
