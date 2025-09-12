import {Schema} from 'effect';
import {type EntryDTO, schemaEntryDto} from './entryDto.ts';
import {Definition, Entry, schemaEntry} from '../../../../domain/Entry.ts';
import {Localized} from '../../../../domain/Localized.ts';
import {LangCode} from '../../../../domain/LangCode.ts';
import {Identifier} from '../../../../domain/identity/Identifier.ts';
import type {Student} from '../../../../domain/Student.ts';

export const entryFromDto: Schema.Schema<Entry, EntryDTO> = Schema.transform(
  schemaEntryDto,
  schemaEntry,
  {
    decode: (dto) =>
      Entry(
        Localized(LangCode(dto.headword.langCode), dto.headword.word),
        dto.definitions.map(d => Definition(Localized(LangCode(d.langCode), d.definition))),
        Identifier<Student>(dto.ownerId)
      ),
    encode: (domain) => ({
      headword: {langCode: domain.word.langCode, word: domain.word.s},
      definitions: domain.definitions.map(d =>
        ({langCode: d.localized.langCode, definition: d.localized.s})),
      ownerId: domain.ownerId
    }),
    strict: true
  }
)

void (entryFromDto satisfies Schema.Schema<Entry, EntryDTO>)
