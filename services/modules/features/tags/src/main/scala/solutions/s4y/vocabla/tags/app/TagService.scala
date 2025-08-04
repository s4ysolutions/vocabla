package solutions.s4y.vocabla.tags.app

import solutions.s4y.vocabla.domain.model.Identifier
import solutions.s4y.vocabla.tags.app.ports.TagUseCases
import solutions.s4y.vocabla.tags.app.repo.{
  TagAssociationRepository,
  TagRepository
}
import solutions.s4y.vocabla.tags.domain.{Owner, Tag, TaggedEntry}
import zio.{IO, ZIO, ZLayer}

// for sake of simplicity all associations needed to be removed when tag is deleted
// should be added to the deleteTag method manually
// see  entriesAssociationRepository.disassociateTagFromAll(tagId))
final class TagService(
    private val tagRepository: TagRepository,
    private val entriesAssociationRepository: TagAssociationRepository[
      TaggedEntry
    ]
) extends TagUseCases:

  override def createTag(
      ownerId: Identifier[Owner],
      tag: Tag
  ): IO[String, Identifier[Tag]] =
    ZIO
      .logDebug(s"Adding tag: $tag for ownerId: $ownerId")
      *> tagRepository.create(ownerId, tag)

  override def deleteTag(
      tagId: Identifier[Tag]
  ): IO[String, Boolean] =
    ZIO.logDebug(s"Removing tag with id: $tagId") *> (tagRepository.delete(
      tagId
    ) <&> entriesAssociationRepository.disassociateTagFromAll(tagId))

end TagService

object TagService:
  def makeLayer(): ZLayer[
    TagRepository & TagAssociationRepository[TaggedEntry],
    Nothing,
    TagService
  ] =
    ZLayer.fromFunction(new TagService(_, _))
end TagService
