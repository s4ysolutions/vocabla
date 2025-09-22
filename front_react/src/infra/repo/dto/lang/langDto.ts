import {Schema} from 'effect';
import type {DeepReadonly} from '../DeepReadonly.ts';
import type {components} from '../../../rest/types.ts';

export type LangDto = DeepReadonly<components['schemas']['Lang']>

export const schemaLangDto = Schema.Struct ({
  code: Schema.String,
  name: Schema.String,
  flag: Schema.String,
});

const _check1: LangDto = {} as Schema.Schema.Type<typeof schemaLangDto>;
void _check1
const _check2: Schema.Schema.Type<typeof schemaLangDto> = {} as LangDto;
void _check2
void ({} as LangDto satisfies Schema.Schema.Type<typeof schemaLangDto>)
void ({} as Schema.Schema.Type<typeof schemaLangDto> satisfies LangDto)
