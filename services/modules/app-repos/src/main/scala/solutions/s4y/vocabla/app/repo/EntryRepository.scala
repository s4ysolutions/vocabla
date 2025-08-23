package solutions.s4y.vocabla.app.repo

import solutions.s4y.vocabla.app.repo.tx.TransactionContext
import solutions.s4y.vocabla.domain.Entry
import solutions.s4y.vocabla.domain.identity.Identifier
import zio.{IO, ZIO}

trait EntryRepository:
  def create(
      entry: Entry
  ): ZIO[TransactionContext, String, Identifier[Entry]]

  def get(
      entryId: Identifier[Entry]
  ): ZIO[TransactionContext, String, Option[Entry]]

  def delete(
      entryId: Identifier[Entry]
  ): ZIO[TransactionContext, String, Boolean]
