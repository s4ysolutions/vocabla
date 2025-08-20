package solutions.s4y.infra.pgsql

import solutions.s4y.vocabla.app.repo.TagRepository
import solutions.s4y.vocabla.domain.Tag
import solutions.s4y.vocabla.domain.identity.{Identified, Identifier}
import zio.{IO, ZIO}

import java.sql.Connection

private final class TagRepositoryPg[OwnerID, TagID] private (
    connection: Connection
) extends TagRepository:
  override def create(
      tag: Tag
  ): IO[String, Identifier[Tag]] = ???
  /*for {
    // TODO: duplicate check
    _ <- ZIO.logDebug(s"Adding tag: $tag")
    _ <- ZIO.blocking{
      val statement = connection.prepareStatement(
        "INSERT INTO vocabla.tags (name, owner_id) VALUES (?, ?)",
        java.sql.Statement.RETURN_GENERATED_KEYS
      )
      statement.setString(1, tag.label)
      statement.setObject(2, tag.ownerId)
      val affectedRows = statement.executeUpdate()
      if (affectedRows == 0) {
        throw new RuntimeException("Creating tag failed, no rows affected.")
      }
      val generatedKeys = statement.getGeneratedKeys
      if (generatedKeys.next()) {
        ZIO.succeed(generatedKeys.getLong(1))
      } else {
        throw new RuntimeException("Creating tag failed, no ID obtained.")
      }
  } yield Identifier(1)*/

  override def update(
      tag: Identified[Tag]
  ): IO[String, Boolean] = ???

  override def delete(
      tagId: Identifier[Tag]
  ): IO[String, Boolean] = ???

  override def get(
      tagId: Identifier[Tag]
  ): IO[String, Option[Tag]] = ???
end TagRepositoryPg
/*
object TagRepositoryMVStore:

  private def apply[OwnerID, TagID](
                                     mvStore: MVStore,
                                     idFactory: IdFactory[TagID]
                                   ): TagRepositoryMVStore[OwnerID, TagID] =
    val mapTags = ZMVMap(
      mvStore.openMap[TagID, TagDTO[OwnerID]]("tags")
    )
    new TagRepositoryMVStore[OwnerID, TagID](mapTags, idFactory)

  def makeLayer[OwnerID: unitOfWork.Tag, TagID: unitOfWork.Tag](): ZLayer[
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

 */
