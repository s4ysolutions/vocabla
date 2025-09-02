import {Schema} from 'effect';

export const schemaStudent = Schema.Struct({
  nickname: Schema.String})

export type Student = Schema.Schema.Type<typeof schemaStudent>
