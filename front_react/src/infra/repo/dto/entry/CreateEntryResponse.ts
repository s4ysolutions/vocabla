import type {components} from '../../../rest/types.ts';
import {Schema} from 'effect';
import type {Entry} from '../../../../domain/Entry.ts';
import {Identifier, schemaIdentifier} from '../../../../domain/identity/Identifier.ts';

export type CreateEntryResponse = components['schemas']['CreateEntryResponse'];

const schemaCreateEntryResponseDto =
  Schema.Struct({
    entryId: Schema.Number
  })

const check1: CreateEntryResponse = {} as Schema.Schema.Type<typeof schemaCreateEntryResponseDto>;
void check1
const check2: Schema.Schema.Type<typeof schemaCreateEntryResponseDto> = {} as CreateEntryResponse;
void check2
void (schemaCreateEntryResponseDto satisfies Schema.Schema<CreateEntryResponse>)

const schemaCreateEntryResponse: Schema.Schema<Identifier<Entry>, CreateEntryResponse> =
  Schema.transform(
    schemaCreateEntryResponseDto,
    schemaIdentifier<Entry>(),
    {
      decode: ({entryId}) => Identifier<Entry>(entryId),
      encode: (id) => ({entryId: id}),
      strict: true,
    })

void (schemaCreateEntryResponse satisfies Schema.Schema<Identifier<Entry>, CreateEntryResponse>)

export const decodeCreateEntryResponse = Schema.decode(schemaCreateEntryResponse)
