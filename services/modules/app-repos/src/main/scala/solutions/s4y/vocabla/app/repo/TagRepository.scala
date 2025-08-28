package solutions.s4y.vocabla.app.repo

import solutions.s4y.vocabla.app.repo.tx.{Transaction, TransactionContext}
import solutions.s4y.vocabla.domain.Tag
import solutions.s4y.vocabla.domain.identity.Identifier
import zio.ZIO

trait TagRepository[TR <: Transaction, TX <: TransactionContext]:
  def create(
      tag: Tag
  ): ZIO[TR & TX, String, Identifier[Tag]]
  def updateLabel(
      id: Identifier[Tag],
      label: String
  ): ZIO[TR & TX, String, Unit]
  def delete(tagId: Identifier[Tag]): ZIO[TR & TX, String, Boolean]
  def get(tagId: Identifier[Tag]): ZIO[TR & TX, String, Option[Tag]]
