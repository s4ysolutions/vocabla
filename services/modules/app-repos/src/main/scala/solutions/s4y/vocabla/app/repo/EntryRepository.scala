package solutions.s4y.vocabla.app.repo

import solutions.s4y.vocabla.app.repo.error.InfraFailure
import solutions.s4y.vocabla.app.repo.tx.TransactionContext
import solutions.s4y.vocabla.domain.Entry
import solutions.s4y.vocabla.domain.identity.Identifier
import zio.ZIO

trait EntryRepository[TX <: TransactionContext]:
  def create[R](
      entry: Entry
  )(using TX): ZIO[R, InfraFailure, Identifier[Entry]]

  def get[R](
      entryId: Identifier[Entry]
  )(using TX): ZIO[R, InfraFailure, Option[Entry]]

  def delete[R](
      entryId: Identifier[Entry]
  )(using TX): ZIO[R, InfraFailure, Boolean]
