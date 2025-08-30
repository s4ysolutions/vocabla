package solutions.s4y.vocabla.app.repo

import solutions.s4y.vocabla.app.repo.error.InfraFailure
import solutions.s4y.vocabla.app.repo.tx.TransactionContext
import solutions.s4y.vocabla.domain.Tag
import solutions.s4y.vocabla.domain.identity.Identifier
import zio.{Chunk, ZIO}

trait TagAssociationRepository[
    TaggedT,
    TX <: TransactionContext
]:

  def associateTagWithEntry[R](
      tagId: Identifier[Tag],
      taggedId: Identifier[TaggedT]
  )(using TX): ZIO[R, InfraFailure, Boolean]

  def disassociateTagFromEntry[R](
      tagId: Identifier[Tag],
      taggId: Identifier[TaggedT]
  )(using TX): ZIO[R, InfraFailure, Boolean]

  def disassociateTagFromAll[R](
      tagId: Identifier[Tag]
  )(using TX): ZIO[R, InfraFailure, Boolean]

  def disassociateTaggedFromAll[R](
      taggedId: Identifier[TaggedT]
  )(using TX): ZIO[R, InfraFailure, Boolean]

  def getTagged[R](
      tagId: Identifier[Tag]
  )(using TX): ZIO[R, InfraFailure, Chunk[Identifier[TaggedT]]]

  def getTags[R](
      taggedId: Identifier[TaggedT]
  )(using TX): ZIO[R, InfraFailure, Chunk[Identifier[Tag]]]
