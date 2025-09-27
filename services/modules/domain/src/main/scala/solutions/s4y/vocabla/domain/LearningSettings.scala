package solutions.s4y.vocabla.domain

import solutions.s4y.vocabla.domain.identity.{
  Identified,
  Identifier,
  IdentifierSchema
}
import zio.Chunk
import zio.json.{DeriveJsonCodec, JsonCodec}

final case class LearningSettings(
    learnLanguages: Chunk[Lang.Code],
    knownLanguages: Chunk[Lang.Code],
    tags: Chunk[Identified[TagSmall]]
)

object LearningSettings:
  val emptyLearningSettings: LearningSettings = LearningSettings(
    learnLanguages = Chunk.empty,
    knownLanguages = Chunk.empty,
    tags = Chunk.empty
  )

  given (using IdentifierSchema): JsonCodec[LearningSettings] =
    DeriveJsonCodec.gen[LearningSettings]
