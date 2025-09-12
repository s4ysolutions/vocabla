import {Schema} from 'effect';
import {Identifier, schemaIdentifier} from '../../../../domain/identity/Identifier.ts';

export const identifierFromNumber = <E>() =>
  Schema.transform(
    Schema.Number,
    schemaIdentifier<E>(),
    {
      decode: (n) => Identifier<E>(n),
      encode: (id) => id,
      strict: true
    }
  )
