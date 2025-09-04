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
export const source = (title: string, url?: string): Source =>
  Schema.decodeSync(schemaSource)({title, url})

export const schemaDefinition = Schema.Struct({
  localized: schemaLocalized,
  source: Schema.optionalWith(schemaSource, {exact: true})
});
export type Definition = Schema.Schema.Type<typeof schemaDefinition>;
export const definition = (localized: Localized) =>
  Schema.decodeSync(schemaDefinition)({localized})

export const schemaEntry = Schema.Struct({
  word: schemaLocalized,
  definitions: Schema.Array(schemaDefinition)
}).pipe(
  Schema.extend(schemaOwned<Student>())
)
export type Entry = Schema.Schema.Type<typeof schemaEntry>
export const entry = (
  word: Localized,
  definitions: Definition[],
  ownerId: Identifier<Student>
): Entry =>
  Schema.decodeSync(schemaEntry)({word, definitions, ownerId})

