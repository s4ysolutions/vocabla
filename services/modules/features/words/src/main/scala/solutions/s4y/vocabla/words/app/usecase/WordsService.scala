package solutions.s4y.vocabla.words.app.usecase

import solutions.s4y.vocabla.words.domain.model.Lang
import zio.IO

trait WordsService[DomainId, OwnerId, EntryId]:
  def newEntry(
      word: String,
      wordLang: Lang.Code,
      definition: String,
      definitionLang: Lang.Code,
      ownerId: OwnerId,
      tagLabels: List[String]
  ): IO[String, DomainId]
