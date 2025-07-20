package solutions.s4y.vocabla.words.app.usecase

import solutions.s4y.vocabla.domain.model.Identifier
import solutions.s4y.vocabla.lang.domain.model.Lang
import solutions.s4y.vocabla.words.app.repo.EntryRepository
import solutions.s4y.vocabla.words.domain.model.{Entry, Owner}
import zio.IO

class WordsServiceLive(
    private val entryRepository: EntryRepository
) extends WordsService:
  override def newEntry(
                         word: String,
                         wordLang: Lang.Code,
                         definition: String,
                         definitionLang: Lang.Code,
                         owner: Identifier[Owner],
                         tagLabels: List[String]
  ): IO[
    String,
    Identifier[Entry]
  ] = entryRepository
    .addEntry(
      owner,
      word,
      wordLang,
      definition,
      definitionLang,
      tagLabels
    )
