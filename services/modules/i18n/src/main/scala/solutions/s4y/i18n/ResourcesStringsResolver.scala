package solutions.s4y.i18n

import java.text.MessageFormat
import java.util.Locale
import scala.collection.mutable
import scala.io.{Codec, Source}

class ResourcesStringsResolver(baseName: String, defaultLocale: Locale)
    extends TranslationResolver:
  private val defaultTranslation: Map[TranslationKey, String] =
    ResourcesStringsTranslations(baseName, defaultLocale)
  private val translations: mutable.Map[Locale, Map[TranslationKey, String]] =
    mutable.Map.empty

  private def translation(key: TranslationKey, locale: Locale): String =
    if locale == defaultLocale then defaultTranslation.getOrElse(key, key.value)
    else
      translations
        .getOrElseUpdate(locale, ResourcesStringsTranslations(baseName, locale))
        .getOrElse(key, defaultTranslation.getOrElse(key, key.value))
  end translation

  override def resolve(
      locale: Locale,
      key: TranslationKey,
      args: Any*
  ): String =
    val pattern = translation(key, locale)
    if args.isEmpty then return pattern
    MessageFormat.format(pattern, args*)
  end resolve
end ResourcesStringsResolver

object ResourcesStringsResolver:
  given default: TranslationResolver =
    new ResourcesStringsResolver("messages", Locale.ENGLISH)
end ResourcesStringsResolver

private object ResourcesStringsTranslations:
  private val extension = "i18n"
  private val equal = """(?<!\\)=""".r
  private given Codec = Codec.UTF8

  private def splitKeyValue(s: String): Option[(TranslationKey, String)] =
    equal
      .findFirstMatchIn(s)
      .map(m =>
        (
          TranslationKey(
            StringContext(s.substring(0, m.start).replace("\\=", "=").trim)
          ),
          s.substring(m.end).trim
        )
      )

  def resourceExists(path: String): Boolean =
    Option(getClass.getResource(path)).isDefined

  private def readTranslationInto(
      path: String,
      target: mutable.Map[TranslationKey, String]
  ): Unit =
    Option(
      getClass.getResourceAsStream(
        if path.startsWith("/") then path else "/" + path
      )
    )
      .map(stream =>
        Source
          .fromInputStream(stream)(using codec = Codec.default)
          .getLines()
          .map(s =>
            if s.isBlank || s.startsWith("#") then None
            else splitKeyValue(s)
          )
          .filter(_.isDefined)
          .map(_.get)
      )
      .getOrElse(
        List.empty[(TranslationKey, String)]
      )
      .foreach(p => target.put(p._1, p._2))

  def apply(baseName: String, locale: Locale): Map[TranslationKey, String] =
    val translations = mutable.Map[TranslationKey, String]()
    if locale.getScript.nonEmpty && locale.getCountry.nonEmpty && locale.getVariant.nonEmpty
    then {
      // Most specific: language + script + country + variant
      readTranslationInto(
        s"${baseName}_${locale.getLanguage}_${locale.getScript}_${locale.getCountry}_${locale.getVariant}.$extension",
        translations
      )
      readTranslationInto(
        s"${baseName}_${locale.getLanguage}_${locale.getScript}_${locale.getCountry}.$extension",
        translations
      )
      readTranslationInto(
        s"${baseName}_${locale.getLanguage}_${locale.getScript}_${locale.getVariant}.$extension",
        translations
      )
      readTranslationInto(
        s"${baseName}_${locale.getLanguage}_${locale.getCountry}_${locale.getVariant}.$extension",
        translations
      )
      readTranslationInto(
        s"${baseName}_${locale.getLanguage}_${locale.getCountry}.$extension",
        translations
      )
      readTranslationInto(
        s"${baseName}_${locale.getLanguage}_${locale.getVariant}.$extension",
        translations
      )
    } else if locale.getScript.nonEmpty && locale.getCountry.nonEmpty then {
      // language + script + country
      readTranslationInto(
        s"${baseName}_${locale.getLanguage}_${locale.getScript}_${locale.getCountry}.$extension",
        translations
      )
      readTranslationInto(
        s"${baseName}_${locale.getLanguage}_${locale.getScript}.$extension",
        translations
      )
      readTranslationInto(
        s"${baseName}_${locale.getLanguage}_${locale.getCountry}.$extension",
        translations
      )
    } else if locale.getScript.nonEmpty && locale.getVariant.nonEmpty then {
      // language + script + variant
      readTranslationInto(
        s"${baseName}_${locale.getLanguage}_${locale.getScript}_${locale.getVariant}.$extension",
        translations
      )
      readTranslationInto(
        s"${baseName}_${locale.getLanguage}_${locale.getScript}.$extension",
        translations
      )
      readTranslationInto(
        s"${baseName}_${locale.getLanguage}_${locale.getVariant}.$extension",
        translations
      )
    } else if locale.getCountry.nonEmpty && locale.getVariant.nonEmpty then {
      // language + country + variant
      readTranslationInto(
        s"${baseName}_${locale.getLanguage}_${locale.getCountry}_${locale.getVariant}.$extension",
        translations
      )
      readTranslationInto(
        s"${baseName}_${locale.getLanguage}_${locale.getCountry}.$extension",
        translations
      )
      readTranslationInto(
        s"${baseName}_${locale.getLanguage}_${locale.getVariant}.$extension",
        translations
      )
    } else if locale.getScript.nonEmpty then {
      // language + script
      readTranslationInto(
        s"${baseName}_${locale.getLanguage}_${locale.getScript}.$extension",
        translations
      )
    } else if locale.getCountry.nonEmpty then {
      // language + country
      readTranslationInto(
        s"${baseName}_${locale.getLanguage}_${locale.getCountry}.$extension",
        translations
      )
    } else if locale.getVariant.nonEmpty then {
      // language + variant
      readTranslationInto(
        s"${baseName}_${locale.getLanguage}_${locale.getVariant}.$extension",
        translations
      )
    } else {
      readTranslationInto(
        s"${baseName}_${locale.getLanguage}.$extension",
        translations
      )
    }
    translations.toMap
  end apply
end ResourcesStringsTranslations
