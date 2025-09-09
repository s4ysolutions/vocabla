import {Schema} from 'effect';
import {type Identifier, schemaIdentifier} from '../../../../domain/identity/Identifier.ts';

export const identifierFromNumber: <E>() => Schema.Schema<Identifier<E>, number> = <E>() => {
  const schemaIdentifierE = schemaIdentifier<E>()
  return Schema.transform(
    Schema.Number,
    schemaIdentifierE,
    {
      decode: (n) => schemaIdentifierE.make({value: n}),
      encode: (id) => id.value,
      strict: true
    }
  )
}
