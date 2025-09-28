import type {components} from '../../../../rest/types.ts';
import {Schema} from 'effect';

export type TagSmallDto = Readonly<components['schemas']['TagSmall']>;

const schemaTagSmallDto = Schema.Struct({
  label: Schema.String,
})

void (schemaTagSmallDto satisfies Schema.Schema<TagSmallDto, TagSmallDto>)

export default schemaTagSmallDto
