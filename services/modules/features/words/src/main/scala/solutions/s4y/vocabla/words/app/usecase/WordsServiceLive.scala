package solutions.s4y.vocabla.words.app.usecase

import solutions.s4y.vocabla.words.app.repo.{DtoIdToDomainId, EntryRepository}
import solutions.s4y.vocabla.words.app.repo.DtoIdToDomainId.toDomain
import solutions.s4y.vocabla.words.app.repo.dto.EntryDTO
import solutions.s4y.vocabla.words.domain.model.Lang
import solutions.s4y.vocabla.words.domain.model.Lang.Code
import zio.IO

class WordsServiceLive[DomainID, OwnerID, EntryID](
    private val entryRepository: EntryRepository[
      OwnerID,
      EntryID,
      EntryDTO[OwnerID, EntryID]
    ]
)(using DtoIdToDomainId[EntryID, DomainID])
    extends WordsService[DomainID, OwnerID, EntryID]:
  override def newEntry(
      word: String,
      wordLang: Lang.Code,
      definition: String,
      definitionLang: Lang.Code,
      ownerId: OwnerID,
      tagLabels: List[String]
  ): IO[
    String,
    DomainID
  ] = entryRepository
    .addEntry(
      ownerId,
      word,
      wordLang,
      definition,
      definitionLang,
      tagLabels
    )
    .map(entryId => entryId.toDomain[DomainID])
