package solutions.s4y.vocabla.words.app.repo

import solutions.s4y.vocabla.domain.model.{IdentifiedEntity, Identifier}
import solutions.s4y.vocabla.lang.domain.model.Lang
import solutions.s4y.vocabla.words.domain.model.{Entry, Owner}
import zio.{IO, Tag}

trait EntryRepository:
  def getEntriesForOwner(
      owner: Identifier[Owner]
  ): IO[String, Seq[IdentifiedEntity[Entry]]]
  def addEntry(
                ownerId: Identifier[Owner],
                word: String,
                wordLang: Lang.Code,
                definition: String,
                definitionLang: Lang.Code,
                tagLabels: Seq[String]
  ): IO[String, Identifier[Entry]]
