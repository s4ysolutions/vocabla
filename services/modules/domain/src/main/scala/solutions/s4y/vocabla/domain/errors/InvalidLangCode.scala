package solutions.s4y.vocabla.domain.errors

import solutions.s4y.i18n.TranslationTemplate
import solutions.s4y.vocabla.domain.Lang

final case class InvalidLangCode(code: Lang.Code, message: TranslationTemplate)
