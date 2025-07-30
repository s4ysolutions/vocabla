package solutions.s4y.vocabla.words.app.ports

import solutions.s4y.vocabla.domain.model.{Identified, Identifier}
import solutions.s4y.vocabla.lang.domain.model.Lang
import solutions.s4y.vocabla.words.domain.model.{Entry, Owner}
import zio.{Chunk, IO}

trait WordsService:
  def newEntry(entry: Entry): IO[String, Identifier[Entry]]
  def getEntriesForOwner(
      ownerId: Identifier[Owner]
  ): IO[String, Chunk[Identified[Entry]]]
