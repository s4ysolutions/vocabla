package solutions.s4y.vocabla.words.infra.kv.mvstore

import org.h2.mvstore.{MVMap, MVStore}
import solutions.s4y.vocabla.id.IdFactory
import solutions.s4y.vocabla.words.app.repo.EntryRepository
import solutions.s4y.vocabla.words.app.repo.dto.{DefinitionDTO, EntryDTO}
import solutions.s4y.vocabla.words.domain.model.Lang
import zio.{IO, Tag, ZIO, ZLayer}

class MVStoreEntryRepository[OwnerID, EntryID, TagID](
    map: MVMap[OwnerID, Seq[EntryDTO[EntryID, TagID]]],
    idFactory: IdFactory[EntryID],
    tagRepository: MVStoreTagRepository[OwnerID, TagID]
) extends EntryRepository[OwnerID, EntryID, EntryDTO[EntryID, TagID]]:

  override def addEntry(
      ownerId: OwnerID,
      word: String,
      wordLang: Lang.Code,
      definition: String,
      definitionLang: Lang.Code,
      tagLabels: Seq[String]
  ): IO[String, EntryID] = for {
    tags <- tagRepository.getTagsForOwner(ownerId)
    tagIds <- ZIO.foreach(tagLabels)(label =>
      tagRepository.addTag(ownerId, label)
    )
    entryId <- idFactory.next
    entry = EntryDTO(
      entryId,
      word,
      wordLang,
      Seq(DefinitionDTO(definition, definitionLang)),
      tagIds
    )
    entries <- getEntriesForOwner(ownerId)
    _ <- ZIO
      .attempt(map.put(ownerId, entries :+ entry))
      .tapErrorCause(ZIO.logWarningCause(s"Error adding entry $entry", _))
      .mapError(th => s"Error adding entry $entry: ${th.getMessage}")
  } yield entryId

  override def getEntriesForOwner(
      ownerId: OwnerID
  ): IO[String, Seq[EntryDTO[EntryID, TagID]]] =
    ZIO
      .attempt(Option(map.get(ownerId)).getOrElse(Seq.empty))
      .tapErrorCause(cause =>
        ZIO.logWarningCause(s"Error getting entries for owner $ownerId", cause)
      )
      .mapError(th =>
        s"Error getting entries for owner $ownerId: ${th.getMessage}"
      )

object MVStoreEntryRepository:
  def apply[OwnerID, EntryID, TagID](
      mvStore: MVStore,
      idFactory: IdFactory[EntryID],
      tagRepository: MVStoreTagRepository[OwnerID, TagID]
  ): MVStoreEntryRepository[OwnerID, EntryID, TagID] =
    val map =
      mvStore.openMap[OwnerID, Seq[EntryDTO[EntryID, TagID]]]("entries")
    new MVStoreEntryRepository[OwnerID, EntryID, TagID](
      map,
      idFactory,
      tagRepository
    )

  def makeMvStoreLayer[OwnerID: Tag, EntryID: Tag, TagID: Tag]: ZLayer[
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

  def makeLayer[OwnerID: Tag, EntryID: Tag, TagID: Tag]: ZLayer[
    MVStore & MVStoreEntryRepository[OwnerID, EntryID, TagID] &
      IdFactory[EntryID],
    String,
    EntryRepository[OwnerID, EntryID, EntryDTO[EntryID, TagID]]
  ] = ZLayer.fromFunction(
    (repo: MVStoreEntryRepository[OwnerID, EntryID, TagID]) =>
      repo.asInstanceOf[
        EntryRepository[OwnerID, EntryID, EntryDTO[EntryID, TagID]]
      ]
  )
