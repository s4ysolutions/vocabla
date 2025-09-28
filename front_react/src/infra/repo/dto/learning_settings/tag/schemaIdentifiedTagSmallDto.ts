import type {components} from '../../../../rest/types.ts';
import {Schema} from 'effect';
import {schemaTagSmall} from '../../../../../domain/TagSmall.ts';

export type IdentifiedTagSmallDto = Readonly<components['schemas']['IdentifiedTagSmall']>;

const schemaIdentifiedTagSmallDto = Schema.Struct({
  id: Schema.Number,
  e: schemaTagSmall,
})

void (schemaIdentifiedTagSmallDto satisfies Schema.Schema<IdentifiedTagSmallDto, IdentifiedTagSmallDto>)

export default schemaIdentifiedTagSmallDto
