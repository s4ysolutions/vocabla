import {Schema} from 'effect';
import type {LangCode} from '../../../../../domain/LangCode.ts';
import {type TagSmall} from '../../../../../domain/TagSmall.ts';
import {type Identified} from '../../../../../domain/identity/Identified.ts';
import type {LearningSettingsR} from '../../../../../app-repo/LearningSettingsRepository.ts';
import {schemaLangCodeFromDto} from '../../lang/langCodeFromDto.ts';
import schemaIdentifiedTagSmallFromDto from '../tag/schemaIdentifiedTagSmallFromDto.ts';
import type {LearningSettingsDto} from './schemaLearningSettingsDto.ts';

export const schemaLearningSettingsRFromDto = Schema.Struct({
  learnLanguages: Schema.Array(schemaLangCodeFromDto),
  knownLanguages: Schema.Array(schemaLangCodeFromDto),
  tags: Schema.Array(schemaIdentifiedTagSmallFromDto)
}).pipe(
  Schema.rename({
    learnLanguages: 'learnLangCodes',
    knownLanguages: 'knownLangCodes'
  })
);

const _check01: LearningSettingsR = {} as Schema.Schema.Type<typeof schemaLearningSettingsRFromDto>;
void _check01
const _check02: Schema.Schema.Type<typeof schemaLearningSettingsRFromDto> = {} as {
  readonly learnLangCodes: ReadonlyArray<LangCode>,
  readonly knownLangCodes: ReadonlyArray<LangCode>,
  readonly tags: ReadonlyArray<Identified<TagSmall>>
};
void _check02
const _check03: LearningSettingsDto = {} as Schema.Schema.Encoded<typeof schemaLearningSettingsRFromDto>;
void _check03
const _check04: Schema.Schema.Encoded<typeof schemaLearningSettingsRFromDto> = {} as LearningSettingsDto;
void _check04

void (schemaLearningSettingsRFromDto satisfies Schema.Schema<LearningSettingsR, LearningSettingsDto>)
void ({} as {
  readonly learnLangCodes: ReadonlyArray<LangCode>,
  readonly knownLangCodes: ReadonlyArray<LangCode>,
  readonly tags: ReadonlyArray<Identified<TagSmall>>
} satisfies Schema.Schema.Type<typeof schemaLearningSettingsRFromDto>)
void ({} as Schema.Schema.Type<typeof schemaLearningSettingsRFromDto> satisfies {
  readonly learnLangCodes: ReadonlyArray<LangCode>,
  readonly knownLangCodes: ReadonlyArray<LangCode>,
  readonly tags: ReadonlyArray<Identified<TagSmall>>
})
void ({} as LearningSettingsDto satisfies Schema.Schema.Encoded<typeof schemaLearningSettingsRFromDto>)
void ({} as Schema.Schema.Encoded<typeof schemaLearningSettingsRFromDto> satisfies LearningSettingsDto)

//export const decodeLearningSettingsResponse = Schema.decode(schemaLearningSettingsRFromDto);
