import type {components} from '../../../rest/types.ts';
import {Schema} from 'effect';
import {schemaLangCodeDto} from '../lang/langCodeDto.ts';
import type {DeepReadonly} from '../DeepReadonly.ts';

export type DefinitionDTO = DeepReadonly<components['schemas']['Entry']['definitions'][0]>

export const schemaDefinitionDto = Schema.mutable(Schema.Struct({
  definition: Schema.String,
  langCode: schemaLangCodeDto
}))

const _check1: DefinitionDTO = {} as Schema.Schema.Type<typeof schemaDefinitionDto>;
void _check1
const _check2: Schema.Schema.Type<typeof schemaDefinitionDto> = {} as DefinitionDTO;
void _check2
void ({} as DefinitionDTO satisfies Schema.Schema.Type<typeof schemaDefinitionDto>)
void ({} as Schema.Schema.Type<typeof schemaDefinitionDto> satisfies DefinitionDTO)
