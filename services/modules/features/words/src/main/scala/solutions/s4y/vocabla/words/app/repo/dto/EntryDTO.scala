package solutions.s4y.vocabla.words.app.repo.dto

import solutions.s4y.vocabla.words.domain.model.Lang

case class EntryDTO[EntryID, TagID](
    id: EntryID,
    word: String,
    lang: Lang.Code,
    definitions: List[DefinitionDTO],
    tags: List[TagID]
)
