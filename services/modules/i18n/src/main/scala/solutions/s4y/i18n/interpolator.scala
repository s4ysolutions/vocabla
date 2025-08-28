package solutions.s4y.i18n

extension (sc: StringContext)
  def t(args: Any*)(using resolver: TranslationResolver): TranslationTemplate =
    TranslationTemplate(resolver, sc, args.toVector)
