package solutions.s4y.vocabla.app.ports.students.settings.known_lang

import solutions.s4y.vocabla.domain.{Lang, LearningSettings}
import solutions.s4y.vocabla.domain.identity.IdentifierSchema
import zio.schema.annotation.description
import zio.schema.{Schema, derived}

@description("Response containing the code of the removed known language.")
final case class RemoveKnownLangResponse(
    @description("The learning settings associated with the removed known language.")
    learningSettings: LearningSettings
)

object RemoveKnownLangResponse:
  given (using IdentifierSchema): Schema[RemoveKnownLangResponse] = Schema.derived
