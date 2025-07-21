package solutions.s4y.vocabla.words.app.usecase

import solutions.s4y.vocabla.domain.model.{Identified, Identifier}
import solutions.s4y.vocabla.lang.domain.model.Lang
import solutions.s4y.vocabla.words.domain.model.{Entry, Owner}
import zio.{Chunk, IO}

trait WordsService:
  def newEntry(
      word: String,
      wordLang: Lang.Code,
      definition: String,
      definitionLang: Lang.Code,
      ownerId: Identifier[Owner],
      tagLabels: Chunk[String]
  ): IO[String, Identifier[Entry]]

  def getEntriesForOwner(
      ownerId: Identifier[Owner]
  ): IO[String, Chunk[Identified[Entry]]]
