import {Effect, ParseResult, Schema} from 'effect';
import {type OwnerId, schemaOwned} from '../../../domain/mixins/Owned.ts';
import {identifierFromNumber} from './identifierFromNumber.ts';

export const ownedFromNumber = <E>() => {
  type OwnedE = OwnerId<E>
  const schemaOwnedE = schemaOwned<E>()
  type OwnerIdDTO = { ownerId: number }
  const schemaIdentifierFromNumberE = identifierFromNumber<E>();
  const schema: Schema.Schema<OwnedE, OwnerIdDTO> = Schema.transformOrFail(
    Schema.Struct({ownerId: Schema.Number}),
    schemaOwnedE,
    {
      decode: (dto, _, ast) =>
        Schema.decode(schemaIdentifierFromNumberE)(dto.ownerId).pipe(
          Effect.mapError((parseError) =>
            new ParseResult.Type(ast, dto, parseError.message)
          ),
          Effect.map((id) => ({ownerId: id})),
        ),
      encode: (domain, _, ast) =>
        ParseResult.fail(new ParseResult.Forbidden(ast, domain, 'Encoding is not supported')),
      strict: true
    }
  )
  return schema;
}
