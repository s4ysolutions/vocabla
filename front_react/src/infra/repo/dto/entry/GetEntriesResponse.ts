import type {components} from '../../../rest/types.ts';
import {Effect, Schema} from 'effect';
import {Entry} from '../../../../domain/Entry.ts';
import {entryFromDto} from './entryFromDto.ts';
import {Identified} from '../../../../domain/identity/Identified.ts';
import {identifiedFromDto} from '../transformers/identifiedFromDto.ts';
import {type EntryDTO, schemaEntryDto} from './entryDto.ts';
import {schemaIdentifier} from '../../../../domain/identity/Identifier.ts';
import type {ParseError} from 'effect/ParseResult';
import type {DeepReadonly} from '../DeepReadonly.ts';

export type GetEntriesResponse = DeepReadonly<components['schemas']['GetEntriesResponse']>;

//export const schemaGetEntriesResponseDto: Schema.Schema<GetEntriesResponse> =
export const schemaGetEntriesResponseDto =
  Schema.Struct({
    entries: Schema.Array(Schema.Struct({
      id: Schema.Number,
      e: schemaEntryDto
    }))
  })

const _check1: GetEntriesResponse = {} as Schema.Schema.Type<typeof schemaGetEntriesResponseDto>
void _check1
const _check2: Schema.Schema.Type<typeof schemaGetEntriesResponseDto> = {} as GetEntriesResponse
void _check2

void ({} as GetEntriesResponse satisfies Schema.Schema.Type<typeof schemaGetEntriesResponseDto>)
void ({} as Schema.Schema.Type<typeof schemaGetEntriesResponseDto> satisfies GetEntriesResponse)
void (schemaGetEntriesResponseDto satisfies Schema.Schema<GetEntriesResponse>)

const transformer = identifiedFromDto<Entry, EntryDTO>(schemaIdentifier<Entry>(), entryFromDto)
export const schemaGetEntriesResponse =
  Schema.Struct({
    entries: Schema.Array(transformer)
  })


const _check3: { entries: ReadonlyArray<Identified<Entry>> } = {} as Schema.Schema.Type<typeof schemaGetEntriesResponse>
void _check3
const _check4: Schema.Schema.Type<typeof schemaGetEntriesResponse> = {} as { entries: Array<Identified<Entry>> }
void _check4
const _check5: GetEntriesResponse = {} as Schema.Schema.Encoded<typeof schemaGetEntriesResponse>
void _check5
const _check6: Schema.Schema.Encoded<typeof schemaGetEntriesResponse> = {} as GetEntriesResponse
void _check6

void (schemaGetEntriesResponse satisfies Schema.Schema<{readonly entries: ReadonlyArray<Identified<Entry>> }, GetEntriesResponse>)

export const decodeGetEntriesResponse = Schema.decode(schemaGetEntriesResponse)

type Decoder = (input: GetEntriesResponse) => Effect.Effect<{readonly entries: ReadonlyArray<Identified<Entry>> }, ParseError>;
const _checkDecoder: Decoder = decodeGetEntriesResponse;
void _checkDecoder
