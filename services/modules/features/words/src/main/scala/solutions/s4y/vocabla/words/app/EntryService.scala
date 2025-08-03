package solutions.s4y.vocabla.words.app

import solutions.s4y.vocabla.domain.model.{Identified, Identifier}
import solutions.s4y.vocabla.words.app.ports.{
  CreateEntryUseCase,
  GetEntriesUseCase
}
import solutions.s4y.vocabla.words.app.repo.EntryRepository
import solutions.s4y.vocabla.words.domain.model.{Entry, Owner}
import zio.{Chunk, IO}

final class EntryService(
    private val entryRepository: EntryRepository
) extends CreateEntryUseCase,
      GetEntriesUseCase:
  override def create(
      ownerId: Identifier[Owner],
      entry: Entry
  ): IO[String, Identifier[Entry]] =
    entryRepository.create(ownerId, entry)
