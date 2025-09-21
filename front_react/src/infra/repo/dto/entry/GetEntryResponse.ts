import type {components} from '../../../rest/types.ts';
import {Option, Schema} from 'effect';
import {Entry, schemaEntry} from '../../../../domain/Entry.ts';
import {entryFromDto} from './entryFromDto.ts';
import type {DeepReadonly} from '../DeepReadonly.ts';

export type GetEntryResponse = DeepReadonly<components['schemas']['GetEntryResponse']>


const schemaGetEntryResponseDto =
  Schema.Struct({
    entry: Schema.optionalWith(Schema.NullOr(entryFromDto), {exact: true})
  })

void (schemaGetEntryResponseDto satisfies Schema.Schema<{ entry?: Entry | null }, GetEntryResponse>)

export const schemaGetEntryResponse: Schema.Schema<Option.Option<Entry>, GetEntryResponse> =
  Schema.transform(
    schemaGetEntryResponseDto,
    Schema.OptionFromSelf(schemaEntry),
    {
      decode: (response) => Option.fromNullable(response.entry),
      encode: (optEntry) =>
        ({
          entry: Option.getOrNull(optEntry as Option.Option<Entry>)
        }),
      strict: true
    })

void (schemaGetEntryResponse satisfies Schema.Schema<Option.Option<Entry>, GetEntryResponse>)
export const decodeGetEntryResponse = Schema.decode(schemaGetEntryResponse)
export const encodeGetEntryResponse = Schema.encode(schemaGetEntryResponse)
