package solutions.s4y.vocabla.app.repo

import solutions.s4y.vocabla.app.repo.error.InfraFailure
import solutions.s4y.vocabla.app.repo.tx.TransactionContext
import solutions.s4y.vocabla.domain.Tag
import solutions.s4y.vocabla.domain.identity.Identifier
import zio.ZIO

trait TagRepository[TX <: TransactionContext]:
  def create[R](
      tag: Tag
  )(using TX): ZIO[R, InfraFailure, Identifier[Tag]]
  def updateLabel[R](
      id: Identifier[Tag],
      label: String
  )(using TX): ZIO[R, InfraFailure, Boolean]
  def delete[R](tagId: Identifier[Tag])(using TX): ZIO[R, InfraFailure, Boolean]
  def get[R](tagId: Identifier[Tag])(using
      TX
  ): ZIO[R, InfraFailure, Option[Tag]]
