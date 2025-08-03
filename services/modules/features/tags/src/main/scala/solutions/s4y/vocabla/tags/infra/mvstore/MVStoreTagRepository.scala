package solutions.s4y.vocabla.tags.infra.mvstore

import org.h2.mvstore.{MVMap, MVStore}
import solutions.s4y.vocabla.domain.model.{Identified, Identifier}
import solutions.s4y.vocabla.error.e
import solutions.s4y.vocabla.id.IdFactory
import solutions.s4y.vocabla.infrastructure.mvstore.ZMVMap
import solutions.s4y.vocabla.tags.app.repo.TagRepository
import solutions.s4y.vocabla.tags.domain.{Owner, Tag}
import solutions.s4y.vocabla.tags.infra.mvstore.MVStoreTagRepository.TagDTO
import zio.prelude.EqualOps
import zio.{Chunk, IO, ZIO, ZLayer}

private final class MVStoreTagRepository[OwnerID, TagID] private (
    map: ZMVMap[OwnerID, Chunk[TagDTO[OwnerID, TagID]]],
    idFactory: IdFactory[TagID]
) extends TagRepository:
  override def create(
      ownerId: Identifier[Owner],
      tag: Tag
  ): IO[String, Identifier[Tag]] =
    for {
      _ <- ZIO.logDebug(s"Adding tag: $tag for ownerId: $ownerId")
      tags <- get(ownerId.as[OwnerID])
      id <- tags.find(_.asTag === tag) match {
        case Some(existingTag) =>
          ZIO
            .logDebug(s"Tag already exists for ownerId: $ownerId")
            .as(existingTag.id)
        case None =>
          for {
            newTagDTO <- idFactory.next.map(id =>
              TagDTO[OwnerID, TagID](id, tag.label, ownerId.as[OwnerID])
            )
            _ <- set(ownerId, tags :+ newTagDTO)
            _ <- ZIO.logDebug(s"Added tag: $newTagDTO for ownerId: $ownerId")
          } yield newTagDTO.id
      }
    } yield Identifier(id)

  override def delete(
      ownerId: Identifier[Owner],
      tagId: Identifier[Tag]
  ): IO[String, Boolean] = {
    val ownerIdDto = ownerId.as[OwnerID]
    val tagIdDto = tagId.as[TagID]
    for {
      _ <- ZIO.logDebug(s"Removing tag with id: $tagId")
      tags <- get(ownerId.as[OwnerID])
      updatedTags = tags.filterNot(_.id == tagIdDto)
      removed <-
        if (updatedTags.size != tags.size) {
          set(ownerId, updatedTags) *> ZIO
            .logDebug(
              s"Updated tags for ownerId: $ownerId, removed tag with id: $tagId"
            )
            .as(true)
        } else {
          ZIO
            .logDebug(s"Tag with id: $tagId not found for ownerId: $ownerId")
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
    map.get(ownerId).map {
      case Some(tags) => tags
      case None       => Chunk.empty[TagDTO[OwnerID, TagID]]
    }

  private def set(
      owner: Identifier[Owner],
      tagDTOs: Chunk[TagDTO[OwnerID, TagID]]
  ): IO[String, Unit] =
    map.put(owner.as[OwnerID], tagDTOs).unit
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
    val map = mvStore.openMap[OwnerID, Chunk[TagDTO[OwnerID, TagID]]]("tags")
    new MVStoreTagRepository[OwnerID, TagID](ZMVMap(map), idFactory)

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
