package solutions.s4y.vocabla.words.infra.mvstore

import org.h2.mvstore.{MVMap, MVStore}
import solutions.s4y.vocabla.domain.model.{Identified, Identifier}
import solutions.s4y.vocabla.id.IdFactory
import solutions.s4y.vocabla.tags.domain.model.Tag
import solutions.s4y.vocabla.tags.infra.mvstore.MVStoreTagRepository
import solutions.s4y.vocabla.words.app.repo.EntryRepository
import solutions.s4y.vocabla.words.domain.model.*
import solutions.s4y.vocabla.words.infra.mvstore.MVStoreEntryRepository.EntryDTO
import zio.prelude.*
import zio.{Chunk, IO, ZIO, ZLayer}

private final class MVStoreEntryRepository[DtoID] private (
    map: MVMap[DtoID, Chunk[EntryDTO[DtoID]]],
    idFactory: IdFactory[DtoID]
)(using e1: Equivalence[Identifier[Tag], DtoID])
    extends EntryRepository:
  override def add(entry: Entry): IO[String, Identifier[Entry]] =
    for {
      entryId <- idFactory.next
      mvsEntry = EntryDTO(entryId, entry)
      mvsOwnerID = entry.owner.as[DtoID]
      mvsEntries <- getMVStoreEntriesForOwner(mvsOwnerID)
      _ <- ZIO
        .attempt(
          map.put(mvsOwnerID, mvsEntries :+ EntryDTO(entryId, entry))
        )
        .tapErrorCause(
          ZIO.logWarningCause(s"Error adding mvsEntry $mvsEntry", _)
        )
        .mapError(th => s"Error adding mvsEntry $mvsEntry: ${th.getMessage}")
    } yield Identifier(entryId)

  override def get(
      owner: Identifier[Owner]
  ): IO[String, Chunk[Identified[Entry]]] =
    ZIO
      .attempt(Option(map.get(owner.as[DtoID])).getOrElse(Chunk.empty))
      .tapErrorCause(cause =>
        ZIO.logWarningCause(s"Error getting entries for owner $owner", cause)
      )
      .mapBoth(
        { th =>
          s"Error getting entries for owner $owner: ${th.getMessage}"
        },
        { mvsEntries =>
          mvsEntries.map { mvsEntry =>
            Identified(
              Identifier(mvsEntry.id),
              Entry(
                Headword(
                  mvsEntry.headword,
                  mvsEntry.wordLang
                ),
                mvsEntry.definitions
                  .map(mvsDefn =>
                    Definition(
                      mvsDefn.definition,
                      mvsDefn.lang
                    )
                  ),
                mvsEntry.tags.map(tagId => Identifier(tagId)),
                owner
              )
            )
          }
        }
      )

  private def getMVStoreEntriesForOwner(
      ownerId: DtoID
  ): IO[String, Chunk[EntryDTO[DtoID]]] =
    ZIO
      .attempt(Option(map.get(ownerId)).getOrElse(Chunk.empty))
      .tapErrorCause(cause =>
        ZIO.logWarningCause(s"Error getting entries for owner $ownerId", cause)
      )
      .mapError(th =>
        s"Error getting entries for owner $ownerId: ${th.getMessage}"
      )

object MVStoreEntryRepository:
  private final case class DefinitionDTO(
      definition: String,
      lang: String
  )

  private final case class EntryDTO[DtoID](
      id: DtoID,
      headword: String,
      wordLang: String,
      definitions: Chunk[DefinitionDTO],
      tags: Chunk[DtoID]
  )

  private object EntryDTO:
    def apply[DtoID](id: DtoID, entry: Entry) =
      new EntryDTO[DtoID](
        id,
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
      mvStore.openMap[DtoID, Chunk[EntryDTO[DtoID]]]("entries")
    new MVStoreEntryRepository[DtoID](
      map,
      idFactory
    )

  def makeLayer[DtoID: zio.Tag]: ZLayer[
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
