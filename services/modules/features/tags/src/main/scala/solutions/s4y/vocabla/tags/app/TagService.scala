package solutions.s4y.vocabla.tags.app

import solutions.s4y.vocabla.domain.model.Identifier
import solutions.s4y.vocabla.tags.app.ports.{AddTagUseCase, RemoveTagUseCase}
import solutions.s4y.vocabla.tags.app.repo.TagRepository
import solutions.s4y.vocabla.tags.domain.{Owner, Tag}
import zio.{IO, ZIO, ZLayer}

class TagService(private val tagRepository: TagRepository)
    extends AddTagUseCase,
      RemoveTagUseCase {

  override def addTag(
      ownerId: Identifier[Owner],
      tag: Tag
  ): IO[String, Identifier[Tag]] =
    ZIO
      .logDebug(s"Adding tag: $tag for ownerId: $ownerId")
      *> tagRepository.add(ownerId, tag)

  override def removeTag(
      ownerId: Identifier[Owner],
      tagId: Identifier[Tag]
  ): IO[String, Boolean] =
    ZIO.logDebug(s"Removing tag with id: $tagId for ownerId: $ownerId") *>
      tagRepository.remove(ownerId, tagId)
}

object TagService:
  def layer: ZLayer[TagRepository, Nothing, TagService] =
    ZLayer.fromFunction(new TagService(_))
