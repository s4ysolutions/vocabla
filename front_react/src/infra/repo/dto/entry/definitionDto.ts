import type {components} from '../../../rest/types.ts';
import {Schema} from 'effect';
import {schemaLangCodeDto} from '../lang/langCodeDto.ts';

export type DefinitionDTO = components['schemas']['Definition']

export const schemaDefinitionDto = Schema.Struct({
  text: Schema.String,
  langCode: schemaLangCodeDto
})
