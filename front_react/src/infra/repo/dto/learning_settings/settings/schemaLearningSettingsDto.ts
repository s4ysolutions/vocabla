import type {DeepReadonly} from '../../DeepReadonly.ts';
import type {components} from '../../../../rest/types.ts';
import {Schema} from 'effect';
import schemaIdentifiedTagSmallDto from '../tag/schemaIdentifiedTagSmallDto.ts';

export type LearningSettingsDto = DeepReadonly<components['schemas']['LearningSettings']>

const schemaLearningSettingsDto = Schema.Struct({
  learnLanguages: Schema.Array(Schema.String),
  knownLanguages: Schema.Array(Schema.String),
  tags: Schema.Array(schemaIdentifiedTagSmallDto)
})

const _check1: LearningSettingsDto = {} as Schema.Schema.Type<typeof schemaLearningSettingsDto>;
void _check1
const _check2: Schema.Schema.Type<typeof schemaLearningSettingsDto> = {} as LearningSettingsDto;
void _check2
const _check3: LearningSettingsDto = {} as Schema.Schema.Encoded<typeof schemaLearningSettingsDto>;
void _check3
const _check4: Schema.Schema.Encoded<typeof schemaLearningSettingsDto> = {} as LearningSettingsDto;
void _check4

void (schemaLearningSettingsDto satisfies Schema.Schema<LearningSettingsDto, LearningSettingsDto>)
void ({} as LearningSettingsDto satisfies Schema.Schema.Type<typeof schemaLearningSettingsDto>)
void ({} as Schema.Schema.Type<typeof schemaLearningSettingsDto> satisfies LearningSettingsDto)
void ({} as LearningSettingsDto satisfies Schema.Schema.Encoded<typeof schemaLearningSettingsDto>)
void ({} as Schema.Schema.Encoded<typeof schemaLearningSettingsDto> satisfies LearningSettingsDto)

export default schemaLearningSettingsDto
