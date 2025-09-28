import type {components} from '../../../rest/types.ts';
import {Schema} from 'effect';
import {schemaLearningSettingsRFromDto} from './settings/schemaLearningSettingsRFromDto.ts';
import type {LearningSettingsR} from '../../../../app-repo/LearningSettingsRepository.ts';
import type {DeepReadonly} from '../DeepReadonly.ts';
import schemaLearningSettingsDto from './settings/schemaLearningSettingsDto.ts';

export type CreateTagResponseDto = DeepReadonly<components['schemas']['CreateTagResponse']>;

const schemaCreateTagResponseDto =
  Schema.Struct({
    tagId: Schema.Number,
    learningSettings: schemaLearningSettingsDto
  })

const schemaCreateTagResponse =
  Schema.transform(
    schemaCreateTagResponseDto,
    schemaLearningSettingsRFromDto,
    {
      decode: (dto) => dto.learningSettings,
      encode: (domain) => ({tagId: 0, learningSettings: domain}),
      strict: true,
    }
  )

void (schemaCreateTagResponse satisfies Schema.Schema<LearningSettingsR, CreateTagResponseDto>)
void ({} as LearningSettingsR satisfies Schema.Schema.Type<typeof schemaCreateTagResponse>)
void ({} as Schema.Schema.Type<typeof schemaCreateTagResponse> satisfies LearningSettingsR)
void ({} as CreateTagResponseDto satisfies Schema.Schema.Encoded<typeof schemaCreateTagResponse>)
void ({} as Schema.Schema.Encoded<typeof schemaCreateTagResponse> satisfies CreateTagResponseDto)


export const decodeCreateTagResponse = Schema.decode(schemaCreateTagResponse)
