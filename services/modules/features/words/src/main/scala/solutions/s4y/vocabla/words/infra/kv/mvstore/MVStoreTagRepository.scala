package solutions.s4y.vocabla.words.infra.kv.mvstore

import org.h2.mvstore.{MVMap, MVStore}
import solutions.s4y.vocabla.domain.model.{Identified, Identifier}
import solutions.s4y.vocabla.id.IdFactory
import solutions.s4y.vocabla.words.app.repo.TagRepository
import solutions.s4y.vocabla.words.domain.model.Tag.equalTag
import solutions.s4y.vocabla.words.domain.model.{Owner, Tag}
import solutions.s4y.vocabla.words.infra.kv.mvstore.MVStoreTagRepository.MVStoreTag
import zio.{Chunk, IO, ZIO, ZLayer}

final class MVStoreTagRepository[OwnerID, TagID](
    map: MVMap[OwnerID, Chunk[MVStoreTag[TagID]]],
    idFactory: IdFactory[TagID]
) extends TagRepository {
  override def add(tag: Tag): IO[String, Identifier[Tag]] = {
    val ownerId: OwnerID = tag.owner.as[OwnerID]
    for {
      tags <-
        ZIO
          .attempt(Option(map.get(ownerId)).getOrElse(Chunk.empty))
          .tapErrorCause(cause =>
            ZIO.logWarningCause(s"Error getting tags for owner $ownerId", cause)
          )
          .mapError(th =>
            s"Error getting tags for owner $ownerId: ${th.getMessage}"
          )
      existingTagOpt = tags.find(_.label == tag.label)
      id <- existingTagOpt match {
        case Some(existingTag) => ZIO.succeed(existingTag.id)
        case None =>
          for {
            newId <- idFactory.next
            newTag = MVStoreTag(newId, tag.label)
            _ <- ZIO
              .attempt(map.put(ownerId, tags :+ newTag))
              .tapErrorCause(cause =>
                ZIO.logWarningCause(s"Error adding tag $newTag", cause)
              )
              .mapError(th => s"Error adding tag $newTag: ${th.getMessage}")
          } yield newId
      }
    } yield Identifier(id)
  }

  override def get(
      owner: Identifier[Owner]
  ): IO[String, Chunk[Identified[Tag]]] = {
    ZIO
      .attempt(Option(map.get(owner.as[OwnerID])).getOrElse(Chunk.empty))
      .tapErrorCause(
        ZIO.logWarningCause(s"Error getting tags for owner $owner", _)
      )
      .mapBoth(
        th => s"Error getting tags for owner $owner: ${th.getMessage}",
        _.map(tag => Identified(Identifier(tag.id), Tag(tag.label, owner)))
      )
  }
}

object MVStoreTagRepository:
  case class MVStoreTag[TagID](id: TagID, label: String)

  def apply[OwnerID, TagID](
      mvStore: MVStore,
      idFactory: IdFactory[TagID]
  ): MVStoreTagRepository[OwnerID, TagID] =
    val map = mvStore.openMap[OwnerID, Chunk[MVStoreTag[TagID]]]("tags")
    new MVStoreTagRepository[OwnerID, TagID](map, idFactory)

  def makeMVstoreLayer[OwnerID: zio.Tag, TagID: zio.Tag]: ZLayer[
    MVStore & IdFactory[TagID],
    String,
    MVStoreTagRepository[OwnerID, TagID]
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

  def makeLayer[OwnerID: zio.Tag, TagID: zio.Tag]: ZLayer[
    MVStore & IdFactory[TagID] & MVStoreTagRepository[OwnerID, TagID],
    String,
    TagRepository
  ] =
    ZLayer.fromFunction(
      (mvStoreTagRepo: MVStoreTagRepository[OwnerID, TagID]) =>
        mvStoreTagRepo
          .asInstanceOf[TagRepository]
    ) >>> makeMVstoreLayer[OwnerID, TagID]
