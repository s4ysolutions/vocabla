package solutions.s4y.vocabla.words.app.usecase

import solutions.s4y.vocabla.words.app.repo.EntryRepository
import solutions.s4y.vocabla.words.domain.model.{Lang, Owner}
import zio.ZIO
/*
def newEntryUseCase(
    word: String,
    wordLang: Lang.Code,
    definition: String,
    definitionLang: Lang.Code,
    ownerId: Owner.Id,
    tagLabels: List[String]
): ZIO[EntryRepository, String, Unit] =
  for {
    repository <- ZIO.service[EntryRepository]
    _ <- repository.addEntry(
      ownerId,
      word,
      wordLang,
      definition,
      definitionLang,
      tagLabels
    )
  } yield ()
 */
