import {Schema} from 'effect';
import {type Localized, schemaLocalized} from './Localized.ts';
import type {Student} from './Student.ts';
import {schemaOwned} from './mixins/Owned.ts';
import type {Identifier} from './identity/Identifier.ts';

export const schemaSource = Schema.Struct({
  title: Schema.String,
  url: Schema.optional(Schema.String)
})
export type Source = typeof schemaSource.Type
// noinspection JSUnusedGlobalSymbols
export const Source = (title: string, url?: string): Source =>
  schemaSource.make({title, url})

export const schemaDefinition = Schema.Struct({
  localized: schemaLocalized,
  source: Schema.optionalWith(schemaSource, {exact: true})
});
export type Definition = Schema.Schema.Type<typeof schemaDefinition>;
export const Definition = (localized: Localized, source?: Source): Definition =>
  source
    ? schemaDefinition.make({localized, source})
    : schemaDefinition.make({localized});

export type Entry = {
  readonly word: Localized,
  readonly definitions: Readonly<Definition[]>,
} & {
  readonly ownerId: Identifier<Student>
}

export const Entry = (
  word: Localized,
  definitions: Readonly<Definition[]>,
  ownerId: Identifier<Student>
): Entry => ({
  word, definitions, ownerId
})

//export const schemaEntry: Schema.Schema<Entry> = Schema.Struct({
export const schemaEntry = Schema.Struct({
  word: schemaLocalized,
  definitions: Schema.Array(schemaDefinition)
}).pipe(
  Schema.extend(schemaOwned<Student>()),
)

const _check1: Entry = {} as Schema.Schema.Type<typeof schemaEntry>;
void _check1
const _check2: Schema.Schema.Type<typeof schemaEntry> = {} as Entry;
void _check2
void ({} as Entry satisfies Schema.Schema.Type<typeof schemaEntry>)
void ({} as Schema.Schema.Type<typeof schemaEntry> satisfies Entry)
