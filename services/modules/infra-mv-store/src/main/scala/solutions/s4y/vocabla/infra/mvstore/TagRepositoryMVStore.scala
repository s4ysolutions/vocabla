package solutions.s4y.vocabla.infra.mvstore

import org.h2.mvstore.MVStore
import solutions.s4y.infra.id.IdFactory
import solutions.s4y.infra.mvstore.ZMVMap
import solutions.s4y.infra.mvstore.assoc.IdToMany
import solutions.s4y.vocabla.app.repo.TagRepository
import solutions.s4y.vocabla.domain.identity.{Identified, Identifier}
import solutions.s4y.vocabla.domain.{Student, Tag}
import solutions.s4y.vocabla.infra.mvstore.dto.TagDTO
import solutions.s4y.zio.e
import zio.{IO, ZIO, ZLayer}

private final class TagRepositoryMVStore[OwnerID, TagID] private (
    mapTags: ZMVMap[TagID, TagDTO[OwnerID]],
    idFactory: IdFactory[TagID]
) extends TagRepository:

  override def create(
      tag: Tag
  ): IO[String, Identifier[Tag]] = for {
    // TODO: duplicate check
    _ <- ZIO.logDebug(s"Adding tag: $tag")
    id <- idFactory.next
    _ <- mapTags.put(
      id,
      TagDTO[OwnerID](tag)
    )
    _ <- ZIO.logDebug(s"Added tag: $tag withId: $id")
  } yield Identifier(id)

  override def update(
      tag: Identified[Tag]
  ): IO[String, Boolean] = {
    // val tagIdDto = tag.id.as[TagID]
    for {
      _ <- ZIO.logDebug(s"Updating tag with id: ${tag.id}")
      updated <- mapTags.get(tag.id.as[TagID]).flatMap {
        case Some(existingTag) =>
          ZIO.logDebug(
            s"Found existing tag ${existingTag} with id: ${tag.id}"
          ) *>
            mapTags.put(
              tag.id.as[TagID],
              TagDTO[OwnerID](tag.e)
            ) *> ZIO.logDebug(s"Updated tag with id: ${tag.id}").as(true)
        case None =>
          ZIO.logWarning(s"Tag with id: ${tag.id} not found").as(false)
      }
    } yield updated
  }

  override def delete(
      tagId: Identifier[Tag]
  ): IO[String, Boolean] = {
    val tagIdDto = tagId.as[TagID]
    for {
      _ <- ZIO.logDebug(s"Removing tag with id: $tagId")
      removed <- mapTags.remove(tagIdDto)
      _ <- ZIO.logDebug(
        if (removed.isDefined) s"Removed tag with id: $tagId"
        else s"Tag with id: $tagId not found"
      )
    } yield removed.isDefined
  }

  override def get(
      tagId: Identifier[Tag]
  ): IO[String, Option[Tag]] = {
    for {
      _ <- ZIO.logDebug(s"Getting tag with id: $tagId")
      result <- mapTags
        .get(tagId.as[TagID])
        .map( // option to dto
          _.map(dto => // dto to Tag
            Tag(dto.label, Identifier[Student, OwnerID](dto.ownerId))
          )
        )
      _ <- ZIO.logDebug(
        if (result.isDefined) s"Found tag with id: $tagId"
        else s"Tag with id: $tagId not found"
      )
    } yield result
  }

end TagRepositoryMVStore

object TagRepositoryMVStore:
  private def apply[OwnerID, TagID](
      mvStore: MVStore,
      idFactory: IdFactory[TagID]
  ): TagRepositoryMVStore[OwnerID, TagID] =
    val mapTags = ZMVMap(
      mvStore.openMap[TagID, TagDTO[OwnerID]]("tags")
    )
    new TagRepositoryMVStore[OwnerID, TagID](mapTags, idFactory)

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
          .attempt(TagRepositoryMVStore[OwnerID, TagID](mvStore, idFactory))
          .e(th => "Error creating TagRepositoryMVStore: " + th.getMessage)
      } yield repo
    }
