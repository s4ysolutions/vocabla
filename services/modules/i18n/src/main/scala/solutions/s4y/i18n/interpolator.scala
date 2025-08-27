package solutions.s4y.i18n

extension (sc: StringContext)
  def t(args: Any*): TranslationTemplate =
    TranslationTemplate(sc, args.toVector)