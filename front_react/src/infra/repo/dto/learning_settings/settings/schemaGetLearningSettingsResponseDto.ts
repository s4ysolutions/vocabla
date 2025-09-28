import type {DeepReadonly} from '../../DeepReadonly.ts';
import type {components} from '../../../../rest/types.ts';
import {Schema} from 'effect';
import {
  schemaLearningSettingsRFromDto
} from './schemaLearningSettingsRFromDto.ts';
import schemaLearningSettingsDto from './schemaLearningSettingsDto.ts';

type GetLearningSettingsResponseDto = DeepReadonly<components['schemas']['GetLearningSettingsResponse']>

const schemaGetLearningSettingsResponseDto =
  Schema.Struct({
    learningSettings: schemaLearningSettingsDto
  })

const _check1: GetLearningSettingsResponseDto = {} as Schema.Schema.Type<typeof schemaGetLearningSettingsResponseDto>;
void _check1
const _check2: Schema.Schema.Type<typeof schemaGetLearningSettingsResponseDto> = {} as GetLearningSettingsResponseDto;
void _check2
const _check3: GetLearningSettingsResponseDto = {} as Schema.Schema.Encoded<typeof schemaGetLearningSettingsResponseDto>;
void _check3
const _check4: Schema.Schema.Encoded<typeof schemaGetLearningSettingsResponseDto> = {} as GetLearningSettingsResponseDto;
void _check4

const schemaLearningSettingsResponse = Schema.transform(
  schemaGetLearningSettingsResponseDto.pick( 'learningSettings'),
  schemaLearningSettingsRFromDto,
  {
    decode: (dto) => dto.learningSettings,
    encode: (domain) => ({learningSettings: domain}),
    strict: true,
  }
)

export const decodeGetLearningSettingsResponse = Schema.decode(schemaLearningSettingsResponse)
