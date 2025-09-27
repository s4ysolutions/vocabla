import {Schema} from 'effect';
import {identifierFromNumber} from '../identifer/identifierFromNumber.ts';

export const ownedFromNumber = <E>() => {
  const schemaIdentifierFromNumberE = identifierFromNumber<E>();
  return  Schema.Struct({
    ownerId: schemaIdentifierFromNumberE
  })
}
