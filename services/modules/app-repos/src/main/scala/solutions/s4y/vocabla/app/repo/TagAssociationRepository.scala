package solutions.s4y.vocabla.app.repo

import solutions.s4y.vocabla.app.repo.tx.{Transaction, TransactionContext}
import solutions.s4y.vocabla.domain.Tag
import solutions.s4y.vocabla.domain.identity.Identifier
import zio.{Chunk, ZIO}

trait TagAssociationRepository[
    TaggedT,
    TR <: Transaction,
    TX <: TransactionContext
]:

  def associateTagWithEntry(
      tagId: Identifier[Tag],
      taggedId: Identifier[TaggedT]
  ): ZIO[TR & TX, String, Boolean]

  def disassociateTagFromEntry(
      tagId: Identifier[Tag],
      taggId: Identifier[TaggedT]
  ): ZIO[TR & TX, String, Boolean]

  def disassociateTagFromAll(
      tagId: Identifier[Tag]
  ): ZIO[TR & TX, String, Boolean]

  def disassociateTaggedFromAll(
      taggedId: Identifier[TaggedT]
  ): ZIO[TR & TX, String, Boolean]

  def getTagged(
      tagId: Identifier[Tag]
  ): ZIO[TR & TX, String, Chunk[Identifier[TaggedT]]]

  def getTags(
      taggedId: Identifier[TaggedT]
  ): ZIO[TR & TX, String, Chunk[Identifier[Tag]]]
