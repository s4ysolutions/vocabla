package solutions.s4y.vocabla.infra.mvstore

import org.h2.mvstore.MVStore
import solutions.s4y.infra.id.IdFactory
import solutions.s4y.infra.mvstore.ZMVMap
import solutions.s4y.vocabla.app.repo.EntryRepository
import solutions.s4y.vocabla.domain.Entry
import solutions.s4y.vocabla.domain.identity.{Identified, Identifier}
import solutions.s4y.vocabla.infra.mvstore.dto.EntryDTO
import solutions.s4y.zio.e
import zio.{IO, ZIO, ZLayer}

private final class EntryRepositoryMVStore[OwnerID, EntryID] private (
    mapEntries: ZMVMap[EntryID, EntryDTO[OwnerID]],
    idFactory: IdFactory[EntryID]
) extends EntryRepository:

  override def createEntry(
      entry: Entry
  ): IO[String, Identifier[Entry]] = for {
    _ <- ZIO.logDebug(s"Adding entry: $entry")
    id <- idFactory.next
    _ <- mapEntries.put(id, EntryDTO[OwnerID](entry))
    _ <- ZIO.logDebug(s"Added entry: $entry withId: $id")
  } yield Identifier(id)

  override def readEntry(
      entryId: Identifier[Entry]
  ): IO[String, Option[Entry]] = for {
    _ <- ZIO.logDebug(s"Getting entry with id: $entryId")
    result <- mapEntries.get(entryId.as[EntryID]).map(_.map(_.asEntry))
    _ <- ZIO.logDebug(
      if (result.isDefined) s"Found entry with id: $entryId"
      else s"Entry with id: $entryId not found"
    )
  } yield result

  override def updateEntry(
      entry: Identified[Entry]
  ): IO[String, Boolean] = for {
    _ <- ZIO.logDebug(s"Updating entry with id: ${entry.id}")
    updated <- mapEntries.get(entry.id.as[EntryID]).flatMap {
      case Some(_) =>
        mapEntries.put(entry.id.as[EntryID], EntryDTO[OwnerID](entry.e)).as(true)
      case None =>
        ZIO.logWarning(s"Entry with id: ${entry.id} not found").as(false)
    }
  } yield updated

  override def deleteEntry(
      entryId: Identifier[Entry]
  ): IO[String, Boolean] = for {
    _ <- ZIO.logDebug(s"Removing entry with id: $entryId")
    removed <- mapEntries.remove(entryId.as[EntryID])
    _ <- ZIO.logDebug(
      if (removed.isDefined) s"Removed entry with id: $entryId"
      else s"Entry with id: $entryId not found"
    )
  } yield removed.isDefined

end EntryRepositoryMVStore

object EntryRepositoryMVStore:
  private def apply[OwnerID, EntryID](
      mvStore: MVStore,
      idFactory: IdFactory[EntryID]
  ): EntryRepositoryMVStore[OwnerID, EntryID] =
    val mapEntries = ZMVMap(
      mvStore.openMap[EntryID, EntryDTO[OwnerID]]("entries")
    )
    new EntryRepositoryMVStore[OwnerID, EntryID](mapEntries, idFactory)

  def makeLayer[OwnerID: zio.Tag, EntryID: zio.Tag](): ZLayer[
    MVStore & IdFactory[EntryID],
    String,
    EntryRepository
  ] =
    ZLayer.fromZIO {
      for {
        mvStore <- ZIO.service[MVStore]
        idFactory <- ZIO.service[IdFactory[EntryID]]
        repo <- ZIO
          .attempt(EntryRepositoryMVStore[OwnerID, EntryID](mvStore, idFactory))
          .e(th => "Error creating EntryRepositoryMVStore: " + th.getMessage)
      } yield repo
    }
