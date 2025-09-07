import {Schema} from 'effect';
import {identifierFromNumber} from './identifierFromNumber.ts';

export const ownedFromNumber = <E>() => {
  const schemaIdentifierFromNumberE = identifierFromNumber<E>();
  return  Schema.Struct({
    ownerId: schemaIdentifierFromNumberE
  })
}
