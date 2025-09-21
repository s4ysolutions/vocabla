import type {components} from '../../../rest/types.ts';
import {Schema} from 'effect';
import type {DeepReadonly} from '../DeepReadonly.ts';

export type HeadwordDTO = DeepReadonly<components['schemas']['Entry']['headword']>

export const schemaHeadwordDto: Schema.Schema<HeadwordDTO> = Schema.Struct({
  word: Schema.String,
  langCode: Schema.String
})

const _check1: HeadwordDTO = {} as Schema.Schema.Type<typeof schemaHeadwordDto>;
void _check1
const _check2: Schema.Schema.Type<typeof schemaHeadwordDto> = {} as HeadwordDTO;
void _check2
void ({} as HeadwordDTO satisfies Schema.Schema.Type<typeof schemaHeadwordDto>)
void ({} as Schema.Schema.Type<typeof schemaHeadwordDto> satisfies HeadwordDTO)

