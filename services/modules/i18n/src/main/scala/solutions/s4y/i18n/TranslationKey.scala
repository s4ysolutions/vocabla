package solutions.s4y.i18n

opaque type TranslationKey = String

object TranslationKey:
  def apply(key: String): TranslationKey = key
  extension (tk: TranslationKey) def value: String = tk
