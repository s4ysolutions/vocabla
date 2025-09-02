import {Schema} from 'effect';
import {schemaIdentifier} from './Identifier.ts';

export const schemaIdentified = <E>(entitySchema: Schema.Schema<E>) => {
  const e = entitySchema
  return Schema.Struct({id: schemaIdentifier<E>(), e})
}
export type Identified<E> = Schema.Schema.Type<ReturnType<typeof schemaIdentified<E>>>
