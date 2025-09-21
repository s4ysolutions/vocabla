import type {components} from '../../../rest/types.ts';
import {Schema} from 'effect';
import {schemaHeadwordDto} from './headwordDto.ts';
import {schemaDefinitionDto} from './definitionDto.ts';
import type {DeepReadonly} from '../DeepReadonly.ts';


export type EntryDTO = DeepReadonly<components['schemas']['Entry']>

//export const schemaEntryDto: Schema.Schema<EntryDTO, EntryDTO> = Schema.Struct({
export const schemaEntryDto = Schema.Struct({
  headword: schemaHeadwordDto,
  definitions: Schema.Array(schemaDefinitionDto),
  ownerId: Schema.Number,
})

const _check1: EntryDTO = {} as Schema.Schema.Type<typeof schemaEntryDto>;
void _check1
const _check2: Schema.Schema.Type<typeof schemaEntryDto> = {} as EntryDTO;
void _check2
void ({} as EntryDTO satisfies Schema.Schema.Type<typeof schemaEntryDto>)
void ({} as Schema.Schema.Type<typeof schemaEntryDto> satisfies EntryDTO)
