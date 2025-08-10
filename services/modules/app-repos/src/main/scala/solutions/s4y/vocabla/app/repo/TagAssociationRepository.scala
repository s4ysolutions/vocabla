package solutions.s4y.vocabla.app.repo

import solutions.s4y.vocabla.domain.Tag
import solutions.s4y.vocabla.domain.identity.Identifier
import zio.IO

trait TagAssociationRepository[TaggedT]:
  def associateTagWithEntry(
      tagId: Identifier[Tag],
      taggedId: Identifier[TaggedT]
  ): IO[String, Unit]

  def disassociateTagFromEntry(
      tagId: Identifier[Tag],
      taggId: Identifier[TaggedT]
  ): IO[String, Unit]

  def disassociateTagFromAll(
      tagId: Identifier[Tag]
  ): IO[String, Unit]

  def disassociateTaggedFromAll(
      taggedId: Identifier[TaggedT]
  ): IO[String, Unit]

  def getTagged(
      tagId: Identifier[Tag]
  ): IO[String, List[Identifier[TaggedT]]]

  def getTags(
      taggedId: Identifier[TaggedT]
  ): IO[String, List[Tag]]
