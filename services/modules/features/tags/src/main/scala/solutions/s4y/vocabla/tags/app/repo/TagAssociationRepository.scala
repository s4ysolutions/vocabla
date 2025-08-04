package solutions.s4y.vocabla.tags.app.repo

import solutions.s4y.vocabla.domain.model.Identifier
import solutions.s4y.vocabla.tags.domain.Tag
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
