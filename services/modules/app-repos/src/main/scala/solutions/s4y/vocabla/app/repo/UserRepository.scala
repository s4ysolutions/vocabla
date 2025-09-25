package solutions.s4y.vocabla.app.repo

import solutions.s4y.vocabla.app.repo.error.InfraFailure
import solutions.s4y.vocabla.app.repo.tx.TransactionContext
import solutions.s4y.vocabla.domain.{LearningSettings, User}
import solutions.s4y.vocabla.domain.identity.Identifier
import zio.ZIO

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
  )(using TX): ZIO[R, InfraFailure, LearningSettings]
end UserRepository

object UserRepository:

end UserRepository
