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

const _check1: Entry = {} as Schema.Schema.Type<typeof entryFromDto>;
void _check1
const _check2: Schema.Schema.Type<typeof entryFromDto> = {} as Entry;
void _check2
const _check3: EntryDTO = {} as Schema.Schema.Encoded<typeof schemaEntryDto>;
void _check3
const _check4: Schema.Schema.Encoded<typeof schemaEntryDto> = {} as EntryDTO;
void _check4

void (entryFromDto satisfies Schema.Schema<Entry, EntryDTO>)
void ({} as Entry satisfies Schema.Schema.Type<typeof entryFromDto>)
void ({} as Schema.Schema.Type<typeof entryFromDto> satisfies Entry)
void ({} as EntryDTO satisfies Schema.Schema.Encoded<typeof schemaEntryDto>)
void ({} as Schema.Schema.Encoded<typeof schemaEntryDto> satisfies EntryDTO)
