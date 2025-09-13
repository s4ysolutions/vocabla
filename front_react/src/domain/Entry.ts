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
export const Definition: (localized: Localized) => Definition = (localized: Localized) =>
  schemaDefinition.make({localized});

export const schemaEntry = Schema.Struct({
  word: schemaLocalized,
  definitions: Schema.Array(schemaDefinition)
}).pipe(
  Schema.extend(schemaOwned<Student>()),
)
export type Entry = Schema.Schema.Type<typeof schemaEntry>
export const Entry = (
  word: Localized,
  definitions: Readonly<Definition[]>,
  ownerId: Identifier<Student>
): Entry => ({
  word, definitions, ownerId
})

