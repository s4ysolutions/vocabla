package solutions.s4y.vocabla.app.repo

import solutions.s4y.vocabla.app.repo.error.InfraFailure
import solutions.s4y.vocabla.app.repo.tx.TransactionContext
import solutions.s4y.vocabla.domain.identity.Identifier
import solutions.s4y.vocabla.domain.{Lang, User}
import zio.ZIO

trait UnknownLanguagesRepository[TX <: TransactionContext]:
  def addKnownLanguage[R](
      studentId: Identifier[User.Student],
      language: Lang.Code
  )(using TX): ZIO[R, InfraFailure, Unit]

  def removeKnownLanguage[R](
      studentId: Identifier[User.Student],
      language: Lang.Code
  )(using TX): ZIO[R, InfraFailure, Unit]
end UnknownLanguagesRepository
