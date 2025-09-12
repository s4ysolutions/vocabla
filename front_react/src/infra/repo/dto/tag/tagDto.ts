import type {components} from '../../../rest/types.ts';
import {Schema} from 'effect';

export type TagDTO = components['schemas']['Tag']

export const schemaTagDto: Schema.Schema<TagDTO> = Schema.Struct({
  label: Schema.String,
  ownerId: Schema.Number
})

type TagDTOFromSchema = Schema.Schema.Type<typeof schemaTagDto>

// compile-time checks
const _check1: TagDTO = {} as TagDTOFromSchema;
void _check1
const _check2: TagDTOFromSchema = {} as TagDTO;
void _check2
void ({} as TagDTO satisfies TagDTOFromSchema)
void ({} as TagDTOFromSchema satisfies TagDTO)
