import {schemaIdentifier} from '../identity/Identifier.ts';
import {Schema} from 'effect';

export const schemaOwned = <E>() => Schema.Struct({
  ownerId: schemaIdentifier<E>()
})
export type OwnerId<E> = Schema.Schema.Type<ReturnType<typeof schemaOwned<E>>>
