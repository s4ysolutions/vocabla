package solutions.s4y.vocabla.app.repo

import solutions.s4y.vocabla.app.repo.tx.{Transaction, TransactionContext}
import solutions.s4y.vocabla.domain.Entry
import solutions.s4y.vocabla.domain.identity.Identifier
import zio.ZIO

trait EntryRepository[TR <: Transaction, TX <: TransactionContext]:
  def create(
      entry: Entry
  ): ZIO[TR & TX, String, Identifier[Entry]]

  def get(
      entryId: Identifier[Entry]
  ): ZIO[TR & TX, String, Option[Entry]]

  def delete(
      entryId: Identifier[Entry]
  ): ZIO[TR & TX, String, Boolean]
