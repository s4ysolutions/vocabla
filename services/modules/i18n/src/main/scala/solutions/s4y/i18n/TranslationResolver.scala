package solutions.s4y.i18n

import java.util.Locale

trait TranslationResolver:
  def resolve(locale: Locale, key: TranslationKey, args: Any*): String
  

