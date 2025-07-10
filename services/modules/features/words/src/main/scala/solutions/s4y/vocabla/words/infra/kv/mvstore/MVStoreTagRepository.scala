package solutions.s4y.vocabla.words.infra.kv.mvstore

import org.h2.mvstore.{MVMap, MVStore}
import solutions.s4y.vocabla.id.IdFactory
import solutions.s4y.vocabla.words.app.repo.TagRepository
import solutions.s4y.vocabla.words.app.repo.dto.TagDTO
import zio.{IO, Tag, ZIO, ZLayer}

class MVStoreTagRepository[OwnerID, TagID](
    map: MVMap[OwnerID, Seq[TagDTO[TagID]]],
    idFactory: IdFactory[TagID]
) extends TagRepository[OwnerID, TagID, TagDTO[TagID]]:

  @Override
  def addTag(
      ownerId: OwnerID,
      label: String
  ): IO[String, TagID] = for {
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
          newTag = TagDTO(newId, label)
          _ <- ZIO
            .attempt(map.put(ownerId, tags :+ newTag))
            .tapErrorCause(cause =>
              ZIO.logWarningCause(s"Error adding tag $newTag", cause)
            )
            .mapError(th => s"Error adding tag $newTag: ${th.getMessage}")
        } yield newId
    }
  } yield id

  @Override
  def getTagsForOwner(ownerId: OwnerID): IO[String, Seq[TagDTO[TagID]]] =
    ZIO
      .attempt(Option(map.get(ownerId)).getOrElse(Seq.empty))
      .tapErrorCause(
        ZIO.logWarningCause(s"Error getting tags for owner $ownerId", _)
      )
      .mapError(th =>
        s"Error getting tags for owner $ownerId: ${th.getMessage}"
      )

object MVStoreTagRepository:
  def apply[OwnerID, TagID: Tag](
      mvStore: MVStore,
      idFactory: IdFactory[TagID]
  ): MVStoreTagRepository[OwnerID, TagID] =
    val map = mvStore.openMap[OwnerID, Seq[TagDTO[TagID]]]("tags")
    new MVStoreTagRepository[OwnerID, TagID](map, idFactory)

  def makeMVstoreLayer[OwnerID: Tag, TagID: Tag]: ZLayer[
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

  def makeLayer[OwnerID: Tag, TagID: Tag]: ZLayer[
    MVStore & IdFactory[TagID] & MVStoreTagRepository[OwnerID, TagID],
    String,
    TagRepository[OwnerID, TagID, TagDTO[TagID]]
  ] =
    ZLayer.fromFunction(
      (mvStoreTagRepo: MVStoreTagRepository[OwnerID, TagID]) =>
        mvStoreTagRepo
          .asInstanceOf[TagRepository[OwnerID, TagID, TagDTO[TagID]]]
    ) >>> makeMVstoreLayer[OwnerID, TagID]
