import type {DeepReadonly} from '../DeepReadonly.ts';
import type {components} from '../../../rest/types.ts';
import {decodeGetLearningSettingsResponse} from './GetLearningSettingsResponse.ts';
import type {ParseOptions} from 'effect/SchemaAST';
import {Effect} from 'effect';
import type {LearningSettingsR} from '../../../../app-repo/LearningSettingsRepository.ts';
import type {ParseError} from 'effect/ParseResult';

export type AddKnownLangResponseDto = DeepReadonly<components['schemas']['AddKnownLangResponse']>

export const decodeAddKnownLangResponse =
  decodeGetLearningSettingsResponse as
    (i: AddKnownLangResponseDto, overrideOptions?: ParseOptions) => Effect.Effect<LearningSettingsR, ParseError, never>
