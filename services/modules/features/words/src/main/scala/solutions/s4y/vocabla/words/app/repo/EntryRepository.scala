package solutions.s4y.vocabla.words.app.repo

import solutions.s4y.vocabla.words.domain.model.{Lang, Owner, Tag}
import solutions.s4y.vocabla.words.infra.kv.mvstore.MVStoreEntryRepository.EntryDTO
import zio.{IO, Task}

trait EntryRepository[OwnerID, EntryID, EntryDTO]:
  def getEntriesForOwner(ownerId: OwnerID): IO[String, Seq[EntryDTO]]
  def addEntry(
      ownerId: OwnerID,
      word: String,
      wordLang: Lang.Code,
      definition: String,
      definitionLang: Lang.Code,
      tagLabels: Seq[String]
  ): IO[String, EntryID]
