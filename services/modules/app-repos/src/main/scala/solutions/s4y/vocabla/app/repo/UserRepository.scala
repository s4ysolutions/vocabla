package solutions.s4y.vocabla.app.repo

import solutions.s4y.vocabla.app.repo.error.InfraFailure
import solutions.s4y.vocabla.app.repo.tx.TransactionContext
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import solutions.s4y.vocabla.domain.{Lang, Tag, User}
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.{Chunk, ZIO}

trait UserRepository[TX <: TransactionContext]:
  /*
  def create(
      student: Student
  ): ZIO[TransactionContext, String, Identifier[Student]]
  def updateNickname(
      id: Identifier[Student],
      nickname: String
  ): ZIO[TransactionContext, String, Unit]
  def delete(
      studentId: Identifier[Student]
  ): ZIO[TransactionContext, String, Boolean]
   */
  def get[R](
      userId: Identifier[User]
  )(using TX): ZIO[R, InfraFailure, Option[User]]

  def getLearningSettings[R](
      studentId: Identifier[User.Student]
  )(using TX): ZIO[R, InfraFailure, UserRepository.LearningSettings]
end UserRepository

object UserRepository:
  final case class LearningSettings(
      learnLanguages: Chunk[Lang.Code],
      knownLanguages: Chunk[Lang.Code],
      tags: Chunk[Identifier[Tag]]
  )

  val emptyLearningSettings: LearningSettings = LearningSettings(
    learnLanguages = Chunk.empty,
    knownLanguages = Chunk.empty,
    tags = Chunk.empty
  )

  given (using IdentifierSchema): JsonCodec[LearningSettings] =
    DeriveJsonCodec.gen[LearningSettings]

end UserRepository
