package solutions.s4y.vocabla.app.repo

import solutions.s4y.vocabla.app.repo.error.InfraFailure
import solutions.s4y.vocabla.app.repo.tx.TransactionContext
import solutions.s4y.vocabla.domain.identity.Identifier
import solutions.s4y.vocabla.domain.{Lang, LearningSettings, User}
import zio.{Chunk, ZIO}

trait LearnLanguagesRepository[TX <: TransactionContext]:
  def addLearnLanguage[R](
      studentId: Identifier[User.Student],
      language: Lang.Code
  )(using TX): ZIO[R, InfraFailure, Unit]

  def removeLearnLanguage[R](
      studentId: Identifier[User.Student],
      language: Lang.Code
  )(using TX): ZIO[R, InfraFailure, Unit]
end LearnLanguagesRepository
