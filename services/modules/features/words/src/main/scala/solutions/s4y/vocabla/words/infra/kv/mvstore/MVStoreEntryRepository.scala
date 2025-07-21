package solutions.s4y.vocabla.words.infra.kv.mvstore

import org.h2.mvstore.{MVMap, MVStore}
import solutions.s4y.vocabla.domain.model.{Identified, Identifier}
import solutions.s4y.vocabla.id.IdFactory
import solutions.s4y.vocabla.words.app.repo.EntryRepository
import solutions.s4y.vocabla.words.domain.model.*
import solutions.s4y.vocabla.words.infra.kv.mvstore.MVStoreEntryRepository.MVStoreEntry
import zio.prelude.*
import zio.{Chunk, IO, ZIO, ZLayer}

final class MVStoreEntryRepository[OwnerID, EntryID, TagID](
    map: MVMap[OwnerID, Chunk[MVStoreEntry[EntryID, TagID]]],
    idFactory: IdFactory[EntryID],
    tagRepository: MVStoreTagRepository[OwnerID, TagID]
)(using e1: Equivalence[Identifier[Tag], TagID])
    extends EntryRepository:
  override def add(entry: Entry): IO[String, Identifier[Entry]] =
    for {
      entryId <- idFactory.next
      mvsEntry = MVStoreEntry(entryId, entry)
      mvsOwnerID = entry.owner.as[OwnerID]
      mvsEntries <- getMVStoreEntriesForOwner(mvsOwnerID)
      _ <- ZIO
        .attempt(
          map.put(mvsOwnerID, mvsEntries :+ MVStoreEntry(entryId, entry))
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
      .attempt(Option(map.get(owner.as[OwnerID])).getOrElse(Chunk.empty))
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
      ownerId: OwnerID
  ): IO[String, Chunk[MVStoreEntry[EntryID, TagID]]] =
    ZIO
      .attempt(Option(map.get(ownerId)).getOrElse(Chunk.empty))
      .tapErrorCause(cause =>
        ZIO.logWarningCause(s"Error getting entries for owner $ownerId", cause)
      )
      .mapError(th =>
        s"Error getting entries for owner $ownerId: ${th.getMessage}"
      )

object MVStoreEntryRepository:
  final case class MVStoreDefinition(
      definition: String,
      lang: String
  )

  final case class MVStoreEntry[EntryID, TagID](
      id: EntryID,
      headword: String,
      wordLang: String,
      definitions: Chunk[MVStoreDefinition],
      tags: Chunk[TagID]
  )

  object MVStoreEntry:
    def apply[EntryID, TagID](id: EntryID, entry: Entry) =
      new MVStoreEntry[EntryID, TagID](
        id,
        entry.headword.word,
        entry.headword.langCode,
        entry.definitions.map(defn =>
          MVStoreDefinition(defn.definition, defn.langCode)
        ),
        entry.tags.map(tagId => tagId.as[TagID])
      )

  def apply[OwnerID, EntryID, TagID](
      mvStore: MVStore,
      idFactory: IdFactory[EntryID],
      tagRepository: MVStoreTagRepository[OwnerID, TagID]
  ): MVStoreEntryRepository[OwnerID, EntryID, TagID] =
    val map =
      mvStore.openMap[OwnerID, Chunk[MVStoreEntry[EntryID, TagID]]]("entries")
    new MVStoreEntryRepository[OwnerID, EntryID, TagID](
      map,
      idFactory,
      tagRepository
    )

  def makeMvStoreLayer[
      OwnerID: zio.Tag,
      EntryID: zio.Tag,
      TagID: zio.Tag
  ]: ZLayer[
    MVStore & MVStoreTagRepository[OwnerID, TagID] & IdFactory[EntryID],
    String,
    MVStoreEntryRepository[OwnerID, EntryID, TagID]
  ] =
    ZLayer.fromZIO {
      for {
        mvStore <- ZIO.service[MVStore]
        idFactory <- ZIO.service[IdFactory[EntryID]]
        tagRepository <- ZIO.service[MVStoreTagRepository[OwnerID, TagID]]
        entryRepository <- ZIO
          .attempt(
            MVStoreEntryRepository(mvStore, idFactory, tagRepository)
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

  def makeLayer[OwnerID: zio.Tag, EntryID: zio.Tag, TagID: zio.Tag]: ZLayer[
    MVStore & MVStoreEntryRepository[OwnerID, EntryID, TagID] &
      IdFactory[EntryID],
    String,
    EntryRepository
  ] = ZLayer.fromFunction(
    (repo: MVStoreEntryRepository[OwnerID, EntryID, TagID]) =>
      repo.asInstanceOf[
        EntryRepository
      ]
  )
