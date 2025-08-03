package solutions.s4y.vocabla.words.app
/*
import solutions.s4y.vocabla.domain.model.{Identified, Identifier}
import solutions.s4y.vocabla.words.app.repo.EntryRepository
import solutions.s4y.vocabla.words.domain.model.{Entry, Owner}
import zio.{Chunk, IO}

final class EntryServiceLive(
    private val entryRepository: EntryRepository
) extends EntryService:
  override def newEntry(entry: Entry): IO[String, Identifier[Entry]] =
    entryRepository.add(entry)

  override def getEntriesForOwner(
      ownerId: Identifier[Owner]
  ): IO[String, Chunk[Identified[Entry]]] =
    entryRepository.get(ownerId)
*/