import {Effect, Schema} from 'effect';
import {schemaOwned} from './mixins/Owned.ts';
import type {Student} from './Student.ts';
import type {Identifier} from './identity/Identifier.ts';
import type {ParseError} from 'effect/ParseResult';

export const schemaTag = Schema.Struct({
  label: Schema.String,
}).pipe(
  Schema.extend(schemaOwned<Student>())
)

export type Tag = typeof schemaTag.Type

export const makeTag = (label: string, ownerId: Identifier<Student>): Tag =>
  Schema.decodeSync(schemaTag)({label, ownerId})

export const makeTagEffect = (label: string, ownerId: Identifier<Student>): Effect.Effect<Tag, ParseError> =>
  Schema.decode(schemaTag)({label, ownerId})
