package solutions.s4y.vocabla.words.app.repo

import solutions.s4y.vocabla.words.domain.model.Lang
import zio.IO

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
