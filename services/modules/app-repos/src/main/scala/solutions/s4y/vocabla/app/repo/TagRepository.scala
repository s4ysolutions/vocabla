package solutions.s4y.vocabla.app.repo

import solutions.s4y.vocabla.app.repo.tx.TransactionContext
import solutions.s4y.vocabla.domain.Tag
import solutions.s4y.vocabla.domain.identity.Identifier
import zio.ZIO

trait TagRepository:
  def create(
      tag: Tag
  ): ZIO[TransactionContext, String, Identifier[Tag]]
  def updateLabel(
      id: Identifier[Tag],
      label: String
  ): ZIO[TransactionContext, String, Unit]
  def delete(tagId: Identifier[Tag]): ZIO[TransactionContext, String, Boolean]
  def get(tagId: Identifier[Tag]): ZIO[TransactionContext, String, Option[Tag]]
