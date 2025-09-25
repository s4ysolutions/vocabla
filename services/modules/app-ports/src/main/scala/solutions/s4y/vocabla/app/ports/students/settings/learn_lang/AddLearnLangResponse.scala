package solutions.s4y.vocabla.app.ports.students.settings.learn_lang

import solutions.s4y.vocabla.domain.Lang
import solutions.s4y.vocabla.domain.identity.IdentifierSchema
import zio.schema.annotation.description
import zio.schema.{Schema, derived}

@description("Response containing the code of the newly added language to learn.")
final case class AddLearnLangResponse(
    @description("Code of the newly added language.")
    langCode: Lang.Code
)

object AddLearnLangResponse:
  given (using IdentifierSchema): Schema[AddLearnLangResponse] = Schema.derived
