package solutions.s4y.vocabla.words.app.ports

import solutions.s4y.vocabla.domain.model.{Identified, Identifier}
import solutions.s4y.vocabla.lang.domain.model.Lang
import solutions.s4y.vocabla.words.domain.model.{Entry, Owner}
import zio.{Chunk, IO}

trait CreateEntryUseCase:
  def create(ownerId: Identifier[Owner], entry: Entry): IO[String, Identifier[Entry]]
