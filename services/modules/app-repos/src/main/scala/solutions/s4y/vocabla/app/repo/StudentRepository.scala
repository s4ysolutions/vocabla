package solutions.s4y.vocabla.app.repo

import solutions.s4y.vocabla.app.repo.tx.TransactionContext
import solutions.s4y.vocabla.domain.Student
import solutions.s4y.vocabla.domain.identity.Identifier
import zio.ZIO

trait StudentRepository:
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
  def get(
      studentId: Identifier[Student]
  ): ZIO[TransactionContext, String, Option[Student]]
