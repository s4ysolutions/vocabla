package solutions.s4y.vocabla.words.infra.kv.mvstore

import org.h2.mvstore.{MVMap, MVStore}
import solutions.s4y.vocabla.id.IdFactory
import solutions.s4y.vocabla.words.app.repo.TagRepository
import solutions.s4y.vocabla.words.infra.kv.mvstore.MVStoreTagRepository.TagDTO
import zio.{IO, ZIO}

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
  case class TagDTO[TagID](id: TagID, label: String)
  def apply[OwnerID, TagID](
      mvStore: MVStore,
      idFactory: IdFactory[TagID]
  ): IO[String, MVStoreTagRepository[OwnerID, TagID]] =
    ZIO
      .attempt {
        val map = mvStore.openMap[OwnerID, Seq[TagDTO[TagID]]]("tags")
        new MVStoreTagRepository[OwnerID, TagID](map, idFactory)
      }
      .tapErrorCause(ZIO.logWarningCause("Error opening MVStoreLive", _))
      .mapError(th => s"Error opening MVStoreLive: ${th.getMessage}")
