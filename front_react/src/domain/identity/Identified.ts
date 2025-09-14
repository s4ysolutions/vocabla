import {Schema} from 'effect';
import {schemaIdentifier} from './Identifier.ts';

export const schemaIdentified =
  <E, EncodedE = E>(entitySchema: Schema.Schema<E, EncodedE>) =>
    Schema.Struct({id: schemaIdentifier<E>(), e: entitySchema})

export type Identified<E, EncodedE=E> = Schema.Schema.Type<ReturnType<typeof schemaIdentified<E, EncodedE>>>
export const Identified = <E>(id: number, e: E): Identified<E> => ({id, e})
