package solutions.s4y.vocabla.words.app.repo

import solutions.s4y.vocabla.domain.model.{Identified, Identifier}
import solutions.s4y.vocabla.words.domain.model.{Entry, Owner}
import zio.{Chunk, IO}

trait EntryRepository:
  def add(entry: Entry): IO[String, Identifier[Entry]]
  def get(owner: Identifier[Owner]): IO[String, Chunk[Identified[Entry]]]
