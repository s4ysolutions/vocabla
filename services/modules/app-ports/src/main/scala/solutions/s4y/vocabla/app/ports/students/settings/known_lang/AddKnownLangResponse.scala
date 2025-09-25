package solutions.s4y.vocabla.app.ports.students.settings.known_lang

import solutions.s4y.vocabla.domain.Lang
import solutions.s4y.vocabla.domain.identity.IdentifierSchema
import zio.schema.annotation.description
import zio.schema.{Schema, derived}

@description("Response containing the code of the newly added known language.")
final case class AddKnownLangResponse(
    @description("Code of the newly added known language.")
    langCode: Lang.Code
)

object AddKnownLangResponse:
  given (using IdentifierSchema): Schema[AddKnownLangResponse] = Schema.derived
