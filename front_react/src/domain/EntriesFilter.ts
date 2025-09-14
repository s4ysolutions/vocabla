import {Option, Schema} from 'effect';
import {type Identifier, schemaIdentifier} from './identity/Identifier.ts';
import type {Tag} from './Tag.ts';
import {type LangCode, schemaLangCode} from './LangCode.ts';

export const schemaEntriesFilter = Schema.Struct({
  tagIds: Schema.Array(schemaIdentifier<Tag>()),
  langCodes: Schema.Array(schemaLangCode),
  text: Schema.Option(Schema.String)
})

export type EntriesFilter = Schema.Schema.Type<typeof schemaEntriesFilter>

export const EntriesFilter = (
  tagIds: ReadonlyArray<Identifier<Tag>>,
  langCodes: ReadonlyArray<LangCode>,
  text: Option.Option<string>
): EntriesFilter =>
  schemaEntriesFilter.make({
    tagIds: tagIds,
    langCodes: langCodes,
    text
  })

