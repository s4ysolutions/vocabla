import type {components} from '../../../rest/types.ts';
import {Schema} from 'effect';

export type TagDTO = components['schemas']['Tag']

export const schemaTagDto : Schema.Schema<TagDTO> = Schema.Struct({
  label: Schema.String,
  ownerId: Schema.Number
})
