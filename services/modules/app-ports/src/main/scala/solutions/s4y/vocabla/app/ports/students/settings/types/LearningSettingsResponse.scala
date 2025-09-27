package solutions.s4y.vocabla.app.ports.students.settings.types

import solutions.s4y.vocabla.domain.identity.IdentifierSchema
import solutions.s4y.vocabla.domain.{Lang, TagSmall}
import zio.Chunk
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.schema.annotation.description
import zio.schema.{DeriveSchema, Schema}

@description("Response containing the learning settings of a student.")
final case class LearningSettingsResponse(
    learnLanguages: Chunk[Lang.Code],
    knownLanguages: Chunk[Lang.Code],
    tags: Chunk[IdentifiedTagSmall]
)

object LearningSettingsResponse:
  given (using IdentifierSchema): Schema[LearningSettingsResponse] =
    DeriveSchema.gen[LearningSettingsResponse]

  given (using IdentifierSchema): JsonCodec[LearningSettingsResponse] =
    DeriveJsonCodec.gen[LearningSettingsResponse]
