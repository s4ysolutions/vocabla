package solutions.s4y.vocabla.tags.infra.mvstore

import org.h2.mvstore.MVStore
import solutions.s4y.vocabla.domain.model.{Identified, Identifier}
import solutions.s4y.vocabla.id.IdFactory
import solutions.s4y.vocabla.infrastructure.mvstore.ZMVMap
import solutions.s4y.vocabla.tags.app.repo.TagRepository
import solutions.s4y.vocabla.tags.domain.{Owner, Tag}
import solutions.s4y.vocabla.tags.infra.mvstore.MVStoreTagRepository.TagDTO
import zio.prelude.EqualOps
import zio.{Chunk, IO, ZIO, ZLayer}

/** A tag can not exist without an owner
  *
  * {ownerId, {tagDTO1, tagDTO2, ...}} {tagId, ownerId}
  */

private final class MVStoreTagRepository[OwnerID, TagID] private (
    mapTagByOwner: ZMVMap[OwnerID, Chunk[TagDTO[OwnerID, TagID]]],
    mapOwnerByTag: ZMVMap[TagID, OwnerID],
    idFactory: IdFactory[TagID]
) extends TagRepository:
  override def create(
      ownerId: Identifier[Owner],
      tag: Tag
  ): IO[String, Identifier[Tag]] = {
    val ownerIdDto = ownerId.as[OwnerID]
    for {
      _ <- ZIO.logDebug(s"Adding tag: $tag for ownerId: $ownerId")
      tags <- get(ownerIdDto)
      id <- tags.find(_.asTag === tag) match {
        case Some(existingTag) =>
          ZIO
            .logDebug(s"Tag already exists for ownerId: $ownerId")
            .as(existingTag.id)
        case None =>
          for {
            newTagDTO <- idFactory.next.map(id =>
              TagDTO[OwnerID, TagID](id, tag.label, ownerIdDto)
            )
            _ <-
              mapOwnerByTag.put(newTagDTO.id, ownerIdDto)
                <&>
                  updateOwnerWithTags(ownerIdDto, tags :+ newTagDTO)
            _ <- ZIO.logDebug(s"Added tag: $newTagDTO for ownerId: $ownerId")
          } yield newTagDTO.id
      }
    } yield Identifier(id)
  }

  override def delete(
      tagId: Identifier[Tag]
  ): IO[String, Boolean] = {
    val tagIdDto = tagId.as[TagID]
    for {
      _ <- ZIO.logDebug(s"Removing tag with id: $tagId")
      ownerIdDto <- mapOwnerByTag.remove(tagIdDto).flatMap {
        case Some(ownerId) => ZIO.succeed(ownerId)
        case None =>
          ZIO.fail(
            s"Tag with id: $tagId does not exist or is not associated with any owner"
          )
      }
      tags <- get(ownerIdDto)
      updatedTags = tags.filterNot(_.id == tagIdDto)
      removed <-
        if (updatedTags.size != tags.size) {
          (mapOwnerByTag.remove(tagIdDto)
            <&>
              updateOwnerWithTags(ownerIdDto, updatedTags))
            *> ZIO
              .logDebug(
                s"Updated tags for ownerId: $ownerIdDto, removed tag with id: $tagId"
              )
              .as(true)
        } else {
          ZIO
            .logDebug(s"Tag with id: $tagId not found for ownerId: $ownerIdDto")
            .as(false)
        }
    } yield removed
  }

  override def get(
      ownerId: Identifier[Owner]
  ): IO[String, Chunk[Identified[Tag]]] = {
    get(ownerId.as[OwnerID])
      .map(_.map(tagDTO => Identified(Identifier(tagDTO.id), tagDTO.asTag)))
  }

  private def get(
      ownerId: OwnerID
  ): IO[String, Chunk[TagDTO[OwnerID, TagID]]] =
    mapTagByOwner.get(ownerId).map {
      case Some(tags) => tags
      case None       => Chunk.empty[TagDTO[OwnerID, TagID]]
    }

  private def updateOwnerWithTags(
      ownerIdDto: OwnerID,
      tagDTOs: Chunk[TagDTO[OwnerID, TagID]]
  ): IO[String, Unit] =
    mapTagByOwner.put(ownerIdDto, tagDTOs).unit

end MVStoreTagRepository

object MVStoreTagRepository:
  private final case class TagDTO[OwnerID, TagID](
      id: TagID,
      label: String,
      ownerID: OwnerID
  )

  extension [OwnerID, TagID](tag: TagDTO[OwnerID, TagID])
    private def asTag: Tag = Tag(tag.label)

  private def apply[OwnerID, TagID](
      mvStore: MVStore,
      idFactory: IdFactory[TagID]
  ): MVStoreTagRepository[OwnerID, TagID] =
    val mapTagsByOwner = ZMVMap(
      mvStore.openMap[OwnerID, Chunk[TagDTO[OwnerID, TagID]]]("tagsByOwner")
    )
    val mapOwnerByTag = ZMVMap(mvStore.openMap[TagID, OwnerID]("ownerByTag"))
    new MVStoreTagRepository[OwnerID, TagID](
      mapTagsByOwner,
      mapOwnerByTag,
      idFactory
    )

  def makeLayer[OwnerID: zio.Tag, TagID: zio.Tag](): ZLayer[
    MVStore & IdFactory[TagID],
    String,
    TagRepository
  ] =
    ZLayer.fromZIO {
      for {
        mvStore <- ZIO.service[MVStore]
        idFactory <- ZIO.service[IdFactory[TagID]]
        repo <- ZIO
          .attempt(MVStoreTagRepository[OwnerID, TagID](mvStore, idFactory))
          .tapErrorCause(cause =>
            ZIO.logErrorCause("Error creating MVStoreTagRepository", cause)
          )
          .mapError(th =>
            s"Error creating MVStoreTagRepository: ${th.getMessage}"
          )
      } yield repo
    }
