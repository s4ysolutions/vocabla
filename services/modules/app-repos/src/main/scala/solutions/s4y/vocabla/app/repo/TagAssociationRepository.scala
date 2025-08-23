package solutions.s4y.vocabla.app.repo

import solutions.s4y.vocabla.app.repo.tx.TransactionContext
import solutions.s4y.vocabla.domain.Tag
import solutions.s4y.vocabla.domain.identity.Identifier
import zio.{Chunk, ZIO}

trait TagAssociationRepository[TaggedT]:
  def associateTagWithEntry(
      tagId: Identifier[Tag],
      taggedId: Identifier[TaggedT]
  ): ZIO[TransactionContext, String, Boolean]

  def disassociateTagFromEntry(
      tagId: Identifier[Tag],
      taggId: Identifier[TaggedT]
  ): ZIO[TransactionContext, String, Boolean]

  def disassociateTagFromAll(
      tagId: Identifier[Tag]
  ): ZIO[TransactionContext, String, Boolean]

  def disassociateTaggedFromAll(
      taggedId: Identifier[TaggedT]
  ): ZIO[TransactionContext, String, Boolean]

  def getTagged(
      tagId: Identifier[Tag]
  ): ZIO[TransactionContext, String, Chunk[Identifier[TaggedT]]]

  def getTags(
      taggedId: Identifier[TaggedT]
  ): ZIO[TransactionContext, String, Chunk[Identifier[Tag]]]
