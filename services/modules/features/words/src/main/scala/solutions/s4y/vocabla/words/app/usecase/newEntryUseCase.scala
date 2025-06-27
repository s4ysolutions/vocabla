package solutions.s4y.vocabla.words.app.usecase

import solutions.s4y.vocabla.words.app.repo.EntryRepository
import solutions.s4y.vocabla.words.app.repo.dto.{EntryDTO, TagDTO}
import solutions.s4y.vocabla.words.domain.model.{Entry, Lang, Owner, Tag}
import zio.ZIO

def newEntryUseCase(
    word: String,
    wordLang: Lang.Code,
    definition: String,
    definitionLang: Lang.Code,
    ownerId: Owner.Id,
    tagLabels: List[String]
): ZIO[
  EntryRepository[Owner.Id, Entry.Id, EntryDTO[Entry.Id, Tag.Id]],
  String,
  Unit
] =
  for {
    repository <- ZIO
      .service[EntryRepository[Owner.Id, Entry.Id, EntryDTO[Entry.Id, Tag.Id]]]
    _ <- repository.addEntry(
      ownerId,
      word,
      wordLang,
      definition,
      definitionLang,
      tagLabels
    )
  } yield ()
