import {schemaOwned} from './mixins/Owned.ts';
import type {Student} from './Student.ts';
import {Schema} from 'effect';
import {type Identifier} from './identity/Identifier.ts';

export const schemaTag = Schema.Struct({
  label: Schema.String,
}).pipe(Schema.extend(schemaOwned<Student>()));

export type Tag = typeof schemaTag.Type

export const tag = (label: string, owner: Identifier<Student>): Tag =>
  Schema.decodeSync(schemaTag)({label, ownerId: owner})
