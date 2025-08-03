package solutions.s4y.vocabla.words.infra.mvstore

import org.h2.mvstore.MVStore
import solutions.s4y.vocabla.domain.model.{Identified, Identifier}
import solutions.s4y.vocabla.id.IdFactory
import solutions.s4y.vocabla.infrastructure.mvstore.SegmentedKey.given
import solutions.s4y.vocabla.infrastructure.mvstore.{
  SkZMVMap,
  ToSegment,
  ZMVMap
}
import solutions.s4y.vocabla.tags.domain.Tag
import solutions.s4y.vocabla.words.app.repo.EntryRepository
import solutions.s4y.vocabla.words.domain.model.*
import solutions.s4y.vocabla.words.infra.mvstore.MVStoreEntryRepository.EntryDTO
import zio.prelude.*
import zio.stream.ZStream
import zio.{Chunk, IO, ZIO, ZLayer}

private final class MVStoreEntryRepository[DtoID: ToSegment] private (
    map: ZMVMap[DtoID, EntryDTO[DtoID]],
    mapByOwner: SkZMVMap[DtoID],
    mapByTag: SkZMVMap[DtoID],
    idFactory: IdFactory[DtoID]
)(using e1: Equivalence[Identifier[Tag], DtoID])
    extends EntryRepository:

  override def put(
      ownerId: Identifier[Owner],
      entry: Entry
  ): IO[String, Identifier[Entry]] =
    val mvsOwnerId = ownerId.as[DtoID]
    for {
      dtoEntryId <- idFactory.next
      dtoEntry = EntryDTO(dtoEntryId, entry)
      _ <- putEntryDTO(dtoEntryId, dtoEntry)
      _ <- associateEntryWithOwner(dtoEntryId, mvsOwnerId)
      _ <- associateEntryWithTags(dtoEntryId, entry.tags.map(_.as[DtoID]))
    } yield Identifier(dtoEntryId)
  end put

  override def get(entryId: Identifier[Entry]): IO[String, Option[Entry]] =
    getEntryByDtoId(entryId.as[DtoID])

  override def getForOwner(
      owner: Identifier[Owner]
  ): ZStream[Any, String, Identified[Entry]] =
    val ownerId = owner.as[DtoID].toString
    mapByOwner
      .cursorOf(ownerId)
      .mapZIO(entryId =>
        getEntryByDtoId(entryId).map {
          _.map(entry => Identified(Identifier(entryId), entry))
        }
      )
      .collectSome
  end getForOwner
  
  override def getForTag(
      tagId: Identifier[Tag]
  ): ZStream[Any, String, Identified[Entry]] =
    val dtoTagId = tagId.as[DtoID].toString
    mapByTag
      .cursorOf(dtoTagId)
      .mapZIO(entryId =>
        getEntryByDtoId(entryId).map {
          _.map(entry => Identified(Identifier(entryId), entry))
        }
      )
      .collectSome
  end getForTag

  override def addTag(
      entryId: Identifier[Entry],
      tagId: Identifier[Tag]
  ): IO[String, Unit] =
    val dtoEntryId = entryId.as[DtoID]
    val dtoTagId = tagId.as[DtoID]
    for {
      entryDTO <- getEntryDtoIdByDtoId(dtoEntryId).flatMap {
        case Some(entry) => ZIO.succeed(entry)
        case None        => ZIO.fail(s"Entry with ID $entryId not found")
      }
      _ <- ZIO.unless(entryDTO.tagIds.contains(dtoTagId)) {
        putEntryDTO(
          dtoEntryId,
          entryDTO.copy(tagIds = entryDTO.tagIds :+ dtoTagId)
        ).zipPar(associateEntryWithTag(dtoEntryId, dtoTagId))
      }
    } yield ()
  end addTag
  
  override def removeTag(
      entryId: Identifier[Entry],
      tagId: Identifier[Tag]
  ): IO[String, Unit] =
    val dtoEntryId = entryId.as[DtoID]
    val dtoTagId = tagId.as[DtoID]
    for {
      entryDTO <- getEntryDtoIdByDtoId(dtoEntryId).flatMap {
        case Some(entry) => ZIO.succeed(entry)
        case None        => ZIO.fail(s"Entry with ID $entryId not found")
      }
      _ <- ZIO.when(entryDTO.tagIds.contains(dtoTagId)) {
        putEntryDTO(
          dtoEntryId,
          entryDTO.copy(tagIds = entryDTO.tagIds.filterNot(_ == dtoTagId))
        ).zipPar(dissociateEntryFromTag(dtoEntryId, dtoTagId))
      }
    } yield ()
  end removeTag

  private def putEntryDTO(
      dtoEntryId: DtoID,
      dtoEntry: EntryDTO[DtoID]
  ): IO[String, Unit] = map.put(dtoEntryId, dtoEntry).unit

  private def associateEntryWithOwner(
      entryId: DtoID,
      ownerId: DtoID
  ): IO[String, Unit] =
    mapByOwner.put(s"$ownerId:$entryId", entryId).unit

  private def associateEntryWithTags(
      entryId: DtoID,
      tagIds: Chunk[DtoID]
  ): IO[String, Unit] = {
    ZIO.foreachParDiscard(tagIds) { tagId =>
      mapByTag.put(tagId :: entryId, tagId)
    }
  }

  private def associateEntryWithTag(
      entryId: DtoID,
      tagId: DtoID
  ): IO[String, Unit] =
    mapByTag.put(tagId :: entryId, entryId).unit

  private def dissociateEntryFromTag(
      entryId: DtoID,
      tagId: DtoID
  ): IO[String, Unit] =
    mapByTag.remove(tagId :: entryId).unit

  private def getEntryDtoIdByDtoId(
      entryId: DtoID
  ): IO[String, Option[EntryDTO[DtoID]]] =
    map.get(entryId)

  private def getEntryByDtoId(entryId: DtoID): IO[String, Option[Entry]] =
    getEntryDtoIdByDtoId(entryId).map(_.map(_.asEntry))

end MVStoreEntryRepository

object MVStoreEntryRepository:
  private final case class DefinitionDTO(
      definition: String,
      lang: String
  )

  private final case class EntryDTO[DtoID](
      ownerId: DtoID,
      headword: String,
      wordLang: String,
      definitions: Chunk[DefinitionDTO],
      tagIds: Chunk[DtoID]
  ):
    def asEntry: Entry =
      Entry(
        Headword(headword, wordLang),
        definitions.map(defn => Definition(defn.definition, defn.lang)),
        tagIds.map(tagId => Identifier(tagId))
      )

    def withTags(tags: Chunk[DtoID]): EntryDTO[DtoID] =
      this.copy(tagIds = tags)
  end EntryDTO

  private object EntryDTO:
    def apply[DtoID](ownerId: DtoID, entry: Entry) =
      new EntryDTO[DtoID](
        ownerId,
        entry.headword.word,
        entry.headword.langCode,
        entry.definitions.map(defn =>
          DefinitionDTO(defn.definition, defn.langCode)
        ),
        entry.tags.map(tagId => tagId.as[DtoID])
      )
  end EntryDTO

  private def apply[DtoID: ToSegment](
      mvStore: MVStore,
      idFactory: IdFactory[DtoID]
  ): MVStoreEntryRepository[DtoID] =
    val map =
      mvStore.openMap[DtoID, EntryDTO[DtoID]]("entries")
    val mapByOwner =
      mvStore.openMap[String, DtoID]("entriesByOwner")
    val mapByTag =
      mvStore.openMap[String, DtoID]("entriesByTag")
    new MVStoreEntryRepository[DtoID](
      ZMVMap(map),
      SkZMVMap(mapByOwner),
      SkZMVMap(mapByTag),
      idFactory
    )
  end apply

  def makeLayer[DtoID: {zio.Tag, ToSegment}](): ZLayer[
    MVStore & IdFactory[DtoID],
    String,
    EntryRepository
  ] = ZLayer.fromZIO {
    for {
      mvStore <- ZIO.service[MVStore]
      idFactory <- ZIO.service[IdFactory[DtoID]]
      entryRepository <- ZIO
        .attempt(
          MVStoreEntryRepository[DtoID](mvStore, idFactory)
        )
        .tapError(th =>
          ZIO.logError(
            s"Error creating MVStoreEntryRepository: ${th.getMessage}"
          )
        )
        .mapError(th =>
          s"Error creating MVStoreEntryRepository: ${th.getMessage}"
        )
    } yield entryRepository
  }
end MVStoreEntryRepository // Companion object
