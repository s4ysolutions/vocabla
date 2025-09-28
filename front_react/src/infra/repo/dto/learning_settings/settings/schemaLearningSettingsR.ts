import {Schema} from 'effect';
import type {LearningSettingsR} from '../../../../../app-repo/LearningSettingsRepository.ts';
import {schemaLangCode} from '../../../../../domain/LangCode.ts';
import {schemaIdentified} from '../../../../../domain/identity/Identified.ts';
import {schemaTagSmall} from '../../../../../domain/TagSmall.ts';

export const schemaLearningSettingsR = Schema.Struct({
  learnLangCodes: Schema.Array(schemaLangCode),
  knownLangCodes: Schema.Array(schemaLangCode),
  tags: Schema.Array(schemaIdentified(schemaTagSmall))
});

const _check1: LearningSettingsR = {} as Schema.Schema.Type<typeof schemaLearningSettingsR>;
void _check1
const _check2: Schema.Schema.Type<typeof schemaLearningSettingsR> = {} as LearningSettingsR;
void _check2
const _check3: LearningSettingsR = {} as Schema.Schema.Encoded<typeof schemaLearningSettingsR>;
void _check3
const _check4: Schema.Schema.Encoded<typeof schemaLearningSettingsR> = {} as LearningSettingsR;
void _check4

void (schemaLearningSettingsR satisfies Schema.Schema<LearningSettingsR, LearningSettingsR>)
void ({} as LearningSettingsR satisfies Schema.Schema.Type<typeof schemaLearningSettingsR>)
void ({} as Schema.Schema.Type<typeof schemaLearningSettingsR> satisfies LearningSettingsR)
void ({} as LearningSettingsR satisfies Schema.Schema.Encoded<typeof schemaLearningSettingsR>)
void ({} as Schema.Schema.Encoded<typeof schemaLearningSettingsR> satisfies LearningSettingsR)
