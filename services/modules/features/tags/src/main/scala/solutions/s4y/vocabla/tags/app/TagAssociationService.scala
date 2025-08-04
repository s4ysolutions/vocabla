package solutions.s4y.vocabla.tags.app

import solutions.s4y.vocabla.domain.model.Identifier
import solutions.s4y.vocabla.tags.app.ports.TagAssociationUseCases
import solutions.s4y.vocabla.tags.app.repo.TagAssociationRepository
import solutions.s4y.vocabla.tags.domain.{Tag, TaggedEntry}
import zio.IO

final class TagAssociationService(
    val entriesRepository: TagAssociationRepository[TaggedEntry]
) extends TagAssociationUseCases:

  override def associateTagWithEntry(
      tagId: Identifier[Tag],
      entryId: Identifier[TaggedEntry]
  ): IO[String, Unit] =
    entriesRepository.associateTagWithEntry(tagId, entryId)

  override def disassociateTagFromEntry(
      tagId: Identifier[Tag],
      entryId: Identifier[TaggedEntry]
  ): IO[String, Unit] =
    entriesRepository.disassociateTagFromEntry(tagId, entryId)

end TagAssociationService

object TagAssociationService:
  def makeLayer(): zio.ZLayer[TagAssociationRepository[
    TaggedEntry
  ], Nothing, TagAssociationService] =
    zio.ZLayer.fromFunction(new TagAssociationService(_))

end TagAssociationService
