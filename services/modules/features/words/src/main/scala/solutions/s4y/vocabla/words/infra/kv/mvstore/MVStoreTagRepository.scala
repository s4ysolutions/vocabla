package solutions.s4y.vocabla.words.infra.kv.mvstore

import org.h2.mvstore.{MVMap, MVStore}
import solutions.s4y.vocabla.domain.model.{IdentifiedEntity, Identity}
import solutions.s4y.vocabla.id.IdFactory
import solutions.s4y.vocabla.words.app.repo.TagRepository
import solutions.s4y.vocabla.domain.model.Identity.IdConverter
import solutions.s4y.vocabla.words.domain.model.Tag.equalTag
import solutions.s4y.vocabla.words.domain.model.{
  Owner,
  Tag
}
import solutions.s4y.vocabla.words.infra.kv.mvstore.MVStoreTagRepository.MVStoreTag
import zio.{IO, ZIO, ZLayer}

final class MVStoreTagRepository[OwnerID: IdConverter, TagID](
    map: MVMap[OwnerID, Seq[MVStoreTag[TagID]]],
    idFactory: IdFactory[TagID]
) extends TagRepository {
  override def addTag(
      owner: Identity[Owner],
      label: String
  ): IO[String, Identity[Tag]] = {
    val ownerId: OwnerID = owner.toId[OwnerID]
    for {
      tags <-
        ZIO
          .attempt(Option(map.get(ownerId)).getOrElse(Seq.empty))
          .tapErrorCause(cause =>
            ZIO.logWarningCause(s"Error getting tags for owner $ownerId", cause)
          )
          .mapError(th =>
            s"Error getting tags for owner $ownerId: ${th.getMessage}"
          )
      existingTagOpt = tags.find(_.label == label)
      id <- existingTagOpt match {
        case Some(existingTag) => ZIO.succeed(existingTag.id)
        case None =>
          for {
            newId <- idFactory.next
            newTag = MVStoreTag(newId, label)
            _ <- ZIO
              .attempt(map.put(ownerId, tags :+ newTag))
              .tapErrorCause(cause =>
                ZIO.logWarningCause(s"Error adding tag $newTag", cause)
              )
              .mapError(th => s"Error adding tag $newTag: ${th.getMessage}")
          } yield newId
      }
    } yield Identity(id)
  }

  override def getTagsForOwner(
      owner: Identity[Owner]
  ): IO[String, Seq[IdentifiedEntity[Tag]]] = {
    ZIO
      .attempt(Option(map.get(owner.toId[OwnerID])).getOrElse(Seq.empty))
      .tapErrorCause(
        ZIO.logWarningCause(s"Error getting tags for owner $owner", _)
      )
      .mapBoth(
        th => s"Error getting tags for owner $owner: ${th.getMessage}",
        _.map(tag => IdentifiedEntity(Identity(tag.id), Tag(tag.label, owner)))
      )
  }
}

object MVStoreTagRepository:
  case class MVStoreTag[TagID](id: TagID, label: String)

  def apply[OwnerID: IdConverter, TagID: zio.Tag](
      mvStore: MVStore,
      idFactory: IdFactory[TagID]
  ): MVStoreTagRepository[OwnerID, TagID] =
    val map = mvStore.openMap[OwnerID, Seq[MVStoreTag[TagID]]]("tags")
    new MVStoreTagRepository[OwnerID, TagID](map, idFactory)

  def makeMVstoreLayer[OwnerID: {zio.Tag, IdConverter}, TagID: zio.Tag]: ZLayer[
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

  def makeLayer[OwnerID: {zio.Tag, IdConverter}, TagID: zio.Tag]: ZLayer[
    MVStore & IdFactory[TagID] & MVStoreTagRepository[OwnerID, TagID],
    String,
    TagRepository
  ] =
    ZLayer.fromFunction(
      (mvStoreTagRepo: MVStoreTagRepository[OwnerID, TagID]) =>
        mvStoreTagRepo
          .asInstanceOf[TagRepository]
    ) >>> makeMVstoreLayer[OwnerID, TagID]
