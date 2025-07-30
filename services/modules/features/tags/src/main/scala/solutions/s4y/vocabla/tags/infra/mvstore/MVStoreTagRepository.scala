package solutions.s4y.vocabla.tags.infra.mvstore

import org.h2.mvstore.{MVMap, MVStore}
import solutions.s4y.vocabla.domain.model.{Identified, Identifier}
import solutions.s4y.vocabla.id.IdFactory
import solutions.s4y.vocabla.tags.app.repo.TagRepository
import solutions.s4y.vocabla.tags.domain.model.{Owner, Tag}
import solutions.s4y.vocabla.tags.infra.mvstore.MVStoreTagRepository.TagDTO
import zio.{Chunk, IO, ZIO, ZLayer}

private final class MVStoreTagRepository[DtoID] private (
    map: MVMap[DtoID, Chunk[TagDTO[DtoID]]],
    idFactory: IdFactory[DtoID]
) extends TagRepository {
  override def add(tag: Tag): IO[String, Identifier[Tag]] = {
    val ownerId: DtoID = tag.owner.as[DtoID]
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
            newTag = TagDTO(newId, tag.label)
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
      .attempt(Option(map.get(owner.as[DtoID])).getOrElse(Chunk.empty))
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
  private final case class TagDTO[DtoID](id: DtoID, label: String)

  private def apply[DtoID](
      mvStore: MVStore,
      idFactory: IdFactory[DtoID]
  ): MVStoreTagRepository[DtoID] =
    val map = mvStore.openMap[DtoID, Chunk[TagDTO[DtoID]]]("tags")
    new MVStoreTagRepository[DtoID](map, idFactory)

  def makeLayer[DtoID: zio.Tag]: ZLayer[
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
