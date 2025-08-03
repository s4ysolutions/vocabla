package solutions.s4y.vocabla.tags.app_given

import solutions.s4y.vocabla.domain.model.Identifier
import solutions.s4y.vocabla.tags.app.repo.TagRepository
import solutions.s4y.vocabla.tags.app_given.ports.TagRequest
import solutions.s4y.vocabla.tags.app_given.ports.TagRequest.{AddTag, RemoveTag}
import solutions.s4y.vocabla.tags.domain.Tag
import zio.ZIO

trait TagUseCase[C <: TagRequest[R], R]:
  def apply(command: C): ZIO[TagRepository, String, R]

object TagUseCase:
  given TagUseCase[AddTag, Identifier[Tag]] with
    def apply(
        command: AddTag
    ): ZIO[TagRepository, String, Identifier[Tag]] =
      val AddTag(ownerId, tag) = command
      ZIO.logDebug(s"Adding tag: $tag for ownerId: $ownerId") *> ZIO
        .serviceWithZIO[TagRepository](_.add(ownerId, tag))

  given TagUseCase[RemoveTag, Boolean] with
    def apply(command: RemoveTag): ZIO[TagRepository, String, Boolean] =
      val RemoveTag(ownerId, tagId) = command
      ZIO.logDebug(s"Removing tag with id: $tagId for ownerId: $ownerId") *>
        ZIO.serviceWithZIO[TagRepository] {
          _.remove(ownerId, tagId)
        }
