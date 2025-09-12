import type {components} from '../../../rest/types.ts';
import {Schema} from 'effect';
import {schemaHeadwordDto} from './headwordDto.ts';
import {schemaDefinitionDto} from './definitionDto.ts';


export type EntryDTO = components['schemas']['Entry']

export const schemaEntryDto: Schema.Schema<EntryDTO, EntryDTO> = Schema.Struct({
  headword: schemaHeadwordDto,
  definitions: Schema.mutable(Schema.Array(schemaDefinitionDto)),
  ownerId: Schema.Number,
})

const _check1: EntryDTO = {} as Schema.Schema.Type<typeof schemaEntryDto>;
void _check1
const _check2: Schema.Schema.Type<typeof schemaEntryDto> = {} as EntryDTO;
void _check2
void ({} as EntryDTO satisfies Schema.Schema.Type<typeof schemaEntryDto>)
void ({} as Schema.Schema.Type<typeof schemaEntryDto> satisfies EntryDTO)
