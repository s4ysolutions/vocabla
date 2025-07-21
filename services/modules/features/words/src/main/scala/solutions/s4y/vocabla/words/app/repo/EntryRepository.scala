package solutions.s4y.vocabla.words.app.repo

import solutions.s4y.vocabla.domain.model.{Identified, Identifier}
import solutions.s4y.vocabla.lang.domain.model.Lang
import solutions.s4y.vocabla.words.domain.model.{Entry, Owner}
import zio.{Chunk, IO, Tag}

trait EntryRepository:
  def getEntriesForOwner(
      owner: Identifier[Owner]
  ): IO[String, Chunk[Identified[Entry]]]
  def addEntry(
      ownerId: Identifier[Owner],
      word: String,
      wordLang: Lang.Code,
      definition: String,
      definitionLang: Lang.Code,
      tagLabels: Chunk[String]
  ): IO[String, Identifier[Entry]]
