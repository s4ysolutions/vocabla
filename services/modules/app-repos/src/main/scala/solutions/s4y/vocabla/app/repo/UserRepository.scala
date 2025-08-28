package solutions.s4y.vocabla.app.repo

import solutions.s4y.vocabla.app.repo.tx.{Transaction, TransactionContext}
import solutions.s4y.vocabla.domain.User
import solutions.s4y.vocabla.domain.identity.Identifier
import zio.ZIO

trait UserRepository[TR <: Transaction, TX <: TransactionContext]:
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
  def get(
      userId: Identifier[User]
  ): ZIO[TR & TX, String, Option[User]]
