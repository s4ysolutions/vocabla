import type {Student} from './Student.ts';
import {type Identifier, schemaIdentifier} from './identity/Identifier.ts';
import {Schema} from 'effect';

export type Tag = {
  label: string,
  ownerId: Identifier<Student>
}

export const Tag = (label: string, ownerId: Identifier<Student>): Tag => ({label, ownerId})

export const schemaTag = Schema.Struct({
  label: Schema.String,
  ownerId: schemaIdentifier<Student>()
})
