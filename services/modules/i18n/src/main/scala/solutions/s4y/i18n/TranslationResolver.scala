package solutions.s4y.i18n

import java.util.Locale

trait TranslationResolver:
  def resolve(key: TranslationKey, args: Array[Any], locale: Locale): String
