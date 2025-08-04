package solutions.s4y.vocabla.tags.app.ports

import solutions.s4y.vocabla.domain.model.Identifier
import solutions.s4y.vocabla.tags.domain.{Tag, TaggedEntry}
import zio.IO

trait TagAssociationUseCases:
  def associateTagWithEntry(
      tagId: Identifier[Tag],
      entryId: Identifier[TaggedEntry]
  ): IO[String, Unit]

  def disassociateTagFromEntry(
      tagId: Identifier[Tag],
      entryId: Identifier[TaggedEntry]
  ): IO[String, Unit]
