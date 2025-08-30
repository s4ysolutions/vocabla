package solutions.s4y.vocabla.infra.mvstore.dto

import solutions.s4y.vocabla.domain.entry.Definition
import solutions.s4y.vocabla.domain.identity.Identifier
import solutions.s4y.vocabla.domain.{Entry, Student}
import zio.Chunk

final case class EntryDTO[OwnerID](
    headword: String,
    headwordLang: String,
    definitions: Chunk[(String, String)],
    ownerID: OwnerID
)

object EntryDTO:
  def apply[OwnerID](entry: Entry): EntryDTO[OwnerID] =
    new EntryDTO(
      entry.headword.word,
      entry.headword.langCode,
      entry.definitions.map(d => (d.definition, d.langCode)),
      entry.ownerId.as[OwnerID]
    )

  extension [OwnerID](entry: EntryDTO[OwnerID])
    def asEntry: Entry =
      Entry(
        headword = solutions.s4y.vocabla.domain.entry.Headword(
          word = entry.headword,
          langCode = entry.headwordLang
        ),
        definitions = entry.definitions.map { case (definition, langCode) =>
          Definition(definition, langCode)
        },
        ownerId = Identifier[Student, OwnerID](entry.ownerID)
      )
