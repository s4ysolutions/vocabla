package solutions.s4y.vocabla.words.infra.kv.mvstore

import org.h2.mvstore.{MVMap, MVStore}
import solutions.s4y.vocabla.id.IdFactory
import solutions.s4y.vocabla.words.app.repo.EntryRepository
import solutions.s4y.vocabla.words.app.repo.dto.{DefinitionDTO, EntryDTO}
import solutions.s4y.vocabla.words.domain.model.Lang
import solutions.s4y.vocabla.words.domain.model.Lang.Code
import zio.{IO, ZIO}

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
    tagIds <- ZIO.foreach(tagLabels)(label => tagRepository.addTag(ownerId, label))
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
  ): IO[String, MVStoreEntryRepository[OwnerID, EntryID, TagID]] =
    ZIO
      .attempt {
        val map =
          mvStore.openMap[OwnerID, Seq[EntryDTO[EntryID, TagID]]]("entries")
        new MVStoreEntryRepository[OwnerID, EntryID, TagID](
          map,
          idFactory,
          tagRepository
        )
      }
      .tapErrorCause(ZIO.logWarningCause("Error opening MVStoreLive", _))
      .mapError(th => s"Error opening MVStoreLive: ${th.getMessage}")
