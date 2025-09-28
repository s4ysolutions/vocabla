import type {DeepReadonly} from '../DeepReadonly.ts';
import type {components} from '../../../rest/types.ts';
import schemaLearningSettingsDto from './settings/schemaLearningSettingsDto.ts';
import {Schema} from 'effect';
import type {LearningSettingsR} from '../../../../app-repo/LearningSettingsRepository.ts';
import {schemaLearningSettingsRFromDto} from './settings/schemaLearningSettingsRFromDto.ts';

export type GetLearningSettingsResponseDto = DeepReadonly<components['schemas']['GetLearningSettingsResponse']>

const schemaGetLearningSettingsResponseDto = Schema.Struct({
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

void (schemaGetLearningSettingsResponseDto satisfies Schema.Schema<GetLearningSettingsResponseDto, GetLearningSettingsResponseDto>)
void ({} as GetLearningSettingsResponseDto satisfies Schema.Schema.Type<typeof schemaGetLearningSettingsResponseDto>)
void ({} as Schema.Schema.Type<typeof schemaGetLearningSettingsResponseDto> satisfies GetLearningSettingsResponseDto)
void ({} as GetLearningSettingsResponseDto satisfies Schema.Schema.Encoded<typeof schemaGetLearningSettingsResponseDto>)
void ({} as Schema.Schema.Encoded<typeof schemaGetLearningSettingsResponseDto> satisfies GetLearningSettingsResponseDto)

const schemaLearningSettingsRFromResponseDto: Schema.Schema<LearningSettingsR, GetLearningSettingsResponseDto> =
  Schema.pluck(schemaGetLearningSettingsResponseDto, 'learningSettings').pipe(
    Schema.compose(schemaLearningSettingsRFromDto)
  );

export const decodeGetLearningSettingsResponse = Schema.decode(schemaLearningSettingsRFromResponseDto)
