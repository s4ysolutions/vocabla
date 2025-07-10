package solutions.s4y.vocabla.words.app.repo

import solutions.s4y.vocabla.words.domain.model.Lang
import zio.{IO, Tag}

/** @tparam OwnerID
  *   type of ID for the owner's ID
  * @tparam EntryID
  *   type of ID for the entry's ID
  * @tparam EntryDTO
  *   a type of DTO used by get*
  */
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
