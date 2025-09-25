package solutions.s4y.vocabla.app.ports.students.settings.learn_lang

import solutions.s4y.vocabla.domain.Lang
import solutions.s4y.vocabla.domain.identity.IdentifierSchema
import zio.schema.annotation.description
import zio.schema.{Schema, derived}

@description("Response containing the code of the removed learning language.")
final case class RemoveLearnLangResponse(
    @description("Code of the removed learning language.")
    langCode: Lang.Code
)

object RemoveLearnLangResponse:
  given (using IdentifierSchema): Schema[RemoveLearnLangResponse] = Schema.derived
