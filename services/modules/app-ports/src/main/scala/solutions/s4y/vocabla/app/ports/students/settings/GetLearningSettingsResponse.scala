package solutions.s4y.vocabla.app.ports.students.settings

import solutions.s4y.vocabla.domain.LearningSettings
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import zio.Chunk
import zio.schema.annotation.description
import zio.schema.{DeriveSchema, Schema}

@description("Response containing the learning settings of a student.")
opaque type GetLearningSettingsResponse = LearningSettings


object GetLearningSettingsResponse:
  def apply(
      value: LearningSettings
  ): GetLearningSettingsResponse = value
  given (using IdentifierSchema): Schema[GetLearningSettingsResponse] =
    DeriveSchema.gen[GetLearningSettingsResponse]
