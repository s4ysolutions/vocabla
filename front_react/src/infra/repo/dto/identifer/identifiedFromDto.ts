import {Schema} from 'effect';
import {type Identifier} from '../../../../domain/identity/Identifier.ts';

export const identifiedFromDto =
  <E, DTO>(schemaIdentifier: Schema.Schema<Identifier<E>, number>, schemaEntity: Schema.Schema<E, DTO>) =>
    Schema.Struct({id: schemaIdentifier, e: schemaEntity})
