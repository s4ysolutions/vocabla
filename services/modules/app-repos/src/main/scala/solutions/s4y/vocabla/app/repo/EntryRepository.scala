package solutions.s4y.vocabla.app.repo

import solutions.s4y.vocabla.app.repo.error.InfraFailure
import solutions.s4y.vocabla.app.repo.tx.TransactionContext
import solutions.s4y.vocabla.domain.{Entry, Lang, Tag, User}
import solutions.s4y.vocabla.domain.identity.{Identified, Identifier}
import zio.{Chunk, ZIO}

trait EntryRepository[TX <: TransactionContext]:
  def create[R](
      entry: Entry
  )(using TX): ZIO[R, InfraFailure, Identifier[Entry]]

  def get[R](
      entryId: Identifier[Entry]
  )(using TX): ZIO[R, InfraFailure, Option[Entry]]

  def get[R](
      ownerId: Option[Identifier[User]] = None,
      tagIds: Chunk[Identifier[Tag]] = Chunk.empty,
      langCodes: Chunk[Lang.Code] = Chunk.empty,
      text: Option[String] = None,
      limit: Int = 100
  )(using TX): ZIO[R, InfraFailure, Chunk[Identified[Entry]]]

  def delete[R](
      entryId: Identifier[Entry]
  )(using TX): ZIO[R, InfraFailure, Boolean]
