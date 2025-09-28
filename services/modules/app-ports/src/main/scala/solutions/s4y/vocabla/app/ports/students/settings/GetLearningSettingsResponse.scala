package solutions.s4y.vocabla.app.ports.students.settings

import solutions.s4y.vocabla.domain.LearningSettings
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import zio.Chunk
import zio.schema.annotation.description
import zio.schema.{DeriveSchema, Schema}

@description("Response containing the learning settings of a student.")
final case class GetLearningSettingsResponse(
    @description(
      "The learning settings of the student."
    )
    learningSettings: LearningSettings
)

object GetLearningSettingsResponse:
  def apply(
      value: LearningSettings
  ): GetLearningSettingsResponse = new GetLearningSettingsResponse(value)
  given (using IdentifierSchema): Schema[GetLearningSettingsResponse] =
    DeriveSchema.gen[GetLearningSettingsResponse]
