package solutions.s4y.i18n

import java.text.MessageFormat
import java.util.Locale
import scala.collection.mutable
import scala.io.{Codec, Source}
private object ResourceTranslations:
  private val extension = "i18n"
  private val equal = """(?<!\\)=""".r
  private given Codec = Codec.UTF8

  private def splitKeyValue(s: String): Option[(TranslationKey, String)] =
    equal
      .findFirstMatchIn(s)
      .map(m =>
        (
          TranslationKey(s.substring(0, m.start).replace("\\=", "=").trim),
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
end ResourceTranslations

class ResourcesResolver(baseName: String, defaultLocale: Locale)
    extends TranslationResolver:
  private val defaultTranslation: Map[TranslationKey, String] =
    ResourceTranslations(baseName, defaultLocale)
  private val translations: mutable.Map[Locale, Map[TranslationKey, String]] =
    mutable.Map.empty

  private def translation(key: TranslationKey, locale: Locale): String =
    if locale == defaultLocale then
      defaultTranslation.getOrElse(key, key.toString + "(not found)")
    else {
      translations
        .getOrElseUpdate(locale, ResourceTranslations(baseName, locale))
        .getOrElse(key, defaultTranslation.getOrElse(key, key.toString))
    }

  def resolve(key: TranslationKey, args: Array[Any], locale: Locale): String =
    val pattern = translation(key, locale)
    if args.length == 0 then return pattern
    val temp = new MessageFormat(pattern)
    temp.format(args)
