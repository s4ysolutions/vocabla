package solutions.s4y.vocabla.tags.infra.mvstore

import org.h2.mvstore.{MVMap, MVStore}
import solutions.s4y.vocabla.domain.model.{Identified, Identifier}
import solutions.s4y.vocabla.id.IdFactory
import solutions.s4y.vocabla.tags.app.repo.TagRepository
import solutions.s4y.vocabla.tags.domain.{Owner, Tag}
import solutions.s4y.vocabla.tags.infra.mvstore.MVStoreTagRepository.TagDTO
import zio.prelude.EqualOps
import zio.{Chunk, IO, ZIO, ZLayer}

private final class MVStoreTagRepository[DtoID] private (
    map: MVMap[DtoID, Chunk[TagDTO[DtoID]]],
    idFactory: IdFactory[DtoID]
) extends TagRepository {
  override def add(
      ownerId: Identifier[Owner],
      tag: Tag
  ): IO[String, Identifier[Tag]] =
    for {
      _ <- ZIO.logDebug(s"Adding tag: $tag for ownerId: $ownerId")
      tags <- get(ownerId.as[DtoID])
      id <- tags.find(_.asTag === tag) match {
        case Some(existingTag) =>
          ZIO
            .logDebug(s"Tag already exists for ownerId: $ownerId")
            .as(existingTag.id)
        case None =>
          for {
            newTagDTO <- idFactory.next.map(id => TagDTO(id, tag.label))
            _ <- set(ownerId, tags :+ newTagDTO)
            _ <- ZIO.logDebug(s"Added tag: $newTagDTO for ownerId: $ownerId")
          } yield newTagDTO.id
      }
    } yield Identifier(id)

  override def remove(
      ownerId: Identifier[Owner],
      tagId: Identifier[Tag]
  ): IO[String, Boolean] = {
    val idDTO = tagId.as[DtoID]
    (for {
      _ <- ZIO.logDebug(s"Removing tag with id: $tagId for ownerId: $ownerId")
      tags <- get(ownerId.as[DtoID])
      updatedTags = tags.filterNot(_.id == idDTO)
      _ <- set(ownerId, updatedTags)
    } yield updatedTags.size < tags.size)
      .tap(removed =>
        if (removed)
          ZIO.logDebug(s"Tag with id: $tagId removed successfully")
        else
          ZIO.logDebug(s"Tag with id: $tagId not found for ownerId: $ownerId")
      )
  }

  override def get(
      ownerId: Identifier[Owner]
  ): IO[String, Chunk[Identified[Tag]]] = {
    get(ownerId.as[DtoID])
      .map(_.map(tagDTO => Identified(Identifier(tagDTO.id), tagDTO.asTag)))
  }

  private def get(
      ownerId: DtoID
  ): IO[String, Chunk[TagDTO[DtoID]]] = {
    ZIO
      .attempt(Option(map.get(ownerId)).getOrElse(Chunk.empty))
      .tapErrorCause(
        ZIO.logWarningCause(s"Error getting tags for ownerId $ownerId", _)
      )
      .mapError(th =>
        s"Error getting tags for ownerId $ownerId: ${th.getMessage}"
      )
  }

  private def set(
      owner: Identifier[Owner],
      tagDTOs: Chunk[TagDTO[DtoID]]
  ): IO[String, Unit] = {
    val ownerId: DtoID = owner.as[DtoID]
    ZIO
      .attempt(map.put(ownerId, tagDTOs))
      .tapErrorCause(cause =>
        ZIO.logWarningCause(s"Error setting tags for ownerId $ownerId", cause)
      )
      .mapError(th =>
        s"Error setting tags for ownerId $ownerId: ${th.getMessage}"
      )
      .unit
  }
}

object MVStoreTagRepository:
  private final case class TagDTO[DtoID](id: DtoID, label: String)

  extension [DtoID](tag: TagDTO[DtoID]) private def asTag: Tag = Tag(tag.label)

  private def apply[DtoID](
      mvStore: MVStore,
      idFactory: IdFactory[DtoID]
  ): MVStoreTagRepository[DtoID] =
    val map = mvStore.openMap[DtoID, Chunk[TagDTO[DtoID]]]("tags")
    new MVStoreTagRepository[DtoID](map, idFactory)

  def makeLayer[DtoID: zio.Tag](): ZLayer[
    MVStore & IdFactory[DtoID],
    String,
    TagRepository
  ] =
    ZLayer.fromZIO {
      for {
        mvStore <- ZIO.service[MVStore]
        idFactory <- ZIO.service[IdFactory[DtoID]]
        repo <- ZIO
          .attempt(MVStoreTagRepository[DtoID](mvStore, idFactory))
          .tapErrorCause(cause =>
            ZIO.logErrorCause("Error creating MVStoreTagRepository", cause)
          )
          .mapError(th =>
            s"Error creating MVStoreTagRepository: ${th.getMessage}"
          )
      } yield repo
    }
