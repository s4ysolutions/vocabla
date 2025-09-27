package solutions.s4y.vocabla.app.ports.students.settings.learn_lang

import solutions.s4y.vocabla.domain.{Lang, LearningSettings}
import solutions.s4y.vocabla.domain.identity.IdentifierSchema
import zio.schema.annotation.description
import zio.schema.{Schema, derived}

@description("Response containing the code of the removed learning language.")
final case class RemoveLearnLangResponse(
    @description("The learning settings associated with the removed learning language.")
    learningSettings: LearningSettings
)

object RemoveLearnLangResponse:
  given (using IdentifierSchema): Schema[RemoveLearnLangResponse] = Schema.derived
