package solutions.s4y.vocabla.words.infra.mvstore

import org.h2.mvstore.{MVMap, MVStore}
import solutions.s4y.vocabla.domain.model.{Identified, Identifier}
import solutions.s4y.vocabla.error.e
import solutions.s4y.vocabla.id.IdFactory
import solutions.s4y.vocabla.infrastructure.mvstore.SegmentedKey.given
import solutions.s4y.vocabla.infrastructure.mvstore.{ToSegment, ZMVMap}
import solutions.s4y.vocabla.tags.domain.Tag
import solutions.s4y.vocabla.tags.infra.mvstore.MVStoreTagRepository
import solutions.s4y.vocabla.words.app.repo.EntryRepository
import solutions.s4y.vocabla.words.domain.model.*
import solutions.s4y.vocabla.words.infra.mvstore.MVStoreEntryRepository.EntryDTO
import zio.prelude.*
import zio.stream.ZStream
import zio.{Chunk, IO, ZIO, ZLayer}

private final class MVStoreEntryRepository[DtoID] private (
    map: ZMVMap[DtoID, EntryDTO[DtoID]],
    mapByOwner: ZMVMap[String, DtoID],
    mapByTag: ZMVMap[DtoID, DtoID],
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
      mapByTag.put(tagId, entryId)
    }
  }

  private def getEntryByDtoId(entryId: DtoID): IO[String, Option[Entry]] =
    map.get(entryId).map(_.map(_.asEntry))

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

  private def apply[DtoID](
      mvStore: MVStore,
      idFactory: IdFactory[DtoID]
  ): MVStoreEntryRepository[DtoID] =
    val map =
      mvStore.openMap[DtoID, EntryDTO[DtoID]]("entries")
    val mapByOwner =
      mvStore.openMap[String, DtoID]("entriesByOwner")
    val mapByTag =
      mvStore.openMap[DtoID, DtoID]("entriesByTag")
    new MVStoreEntryRepository[DtoID](
      ZMVMap(map),
      ZMVMap(mapByOwner),
      ZMVMap(mapByTag),
      idFactory
    )

  def makeLayer[DtoID: zio.Tag](): ZLayer[
    MVStore & IdFactory[DtoID],
    String,
    EntryRepository
  ] =
    ZLayer.fromZIO {
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
