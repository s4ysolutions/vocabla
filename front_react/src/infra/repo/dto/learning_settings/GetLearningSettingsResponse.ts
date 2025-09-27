import type {DeepReadonly} from '../DeepReadonly.ts';
import type {components} from '../../../rest/types.ts';
import {Schema} from 'effect';
import {schemaLangCodeFromDto} from '../lang/langCodeFromDto.ts';
import {identifierFromNumber} from '../identifer/identifierFromNumber.ts';
import type {Tag, TagId} from '../../../../domain/Tag.ts';
import type {LangCode} from '../../../../domain/LangCode.ts';

export type GetLearningSettingsResponseDto = DeepReadonly<components['schemas']['LearningSettings']>

export const schemaLearningSettingsResponseDto = Schema.Struct({
  learnLanguages: Schema.Array(Schema.String),
  knownLanguages: Schema.Array(Schema.String),
  tags: Schema.Array(Schema.Number)
})

const _check1: GetLearningSettingsResponseDto = {} as Schema.Schema.Type<typeof schemaLearningSettingsResponseDto>;
void _check1
const _check2: Schema.Schema.Type<typeof schemaLearningSettingsResponseDto> = {} as GetLearningSettingsResponseDto;
void _check2
const _check3: GetLearningSettingsResponseDto = {} as Schema.Schema.Encoded<typeof schemaLearningSettingsResponseDto>;
void _check3
const _check4: Schema.Schema.Encoded<typeof schemaLearningSettingsResponseDto> = {} as GetLearningSettingsResponseDto;
void _check4

void (schemaLearningSettingsResponseDto satisfies Schema.Schema<GetLearningSettingsResponseDto, GetLearningSettingsResponseDto>)
void ({} as GetLearningSettingsResponseDto satisfies Schema.Schema.Type<typeof schemaLearningSettingsResponseDto>)
void ({} as Schema.Schema.Type<typeof schemaLearningSettingsResponseDto> satisfies GetLearningSettingsResponseDto)
void ({} as GetLearningSettingsResponseDto satisfies Schema.Schema.Encoded<typeof schemaLearningSettingsResponseDto>)
void ({} as Schema.Schema.Encoded<typeof schemaLearningSettingsResponseDto> satisfies GetLearningSettingsResponseDto)

const transform = Schema.transform(
  schemaLearningSettingsResponseDto,
  Schema.Struct({
    learnLangCodes: Schema.Array(schemaLangCodeFromDto),
    knownLangCodes: Schema.Array(schemaLangCodeFromDto),
    tagIds: Schema.Array(identifierFromNumber<Tag>())
  }),
  {
    decode: (dto) => ({
      learnLangCodes: dto.learnLanguages,
      knownLangCodes: dto.knownLanguages,
      tagIds: dto.tags
    }),
    encode: (domain) => ({
      learnLanguages: domain.learnLangCodes,
      knownLanguages: domain.knownLangCodes,
      tags: domain.tagIds
    }),
    strict: true
  }
)

const _check01: {
  readonly learnLangCodes: ReadonlyArray<LangCode>,
  readonly knownLangCodes: ReadonlyArray<LangCode>,
  readonly tagIds: ReadonlyArray<TagId>
} = {} as Schema.Schema.Type<typeof transform>;
void _check01
const _check02: Schema.Schema.Type<typeof transform> = {} as {
  readonly learnLangCodes: ReadonlyArray<LangCode>,
  readonly knownLangCodes: ReadonlyArray<LangCode>,
  readonly tagIds: ReadonlyArray<TagId>
};
void _check02
const _check03: {
  readonly learnLanguages: ReadonlyArray<string>,
  readonly knownLanguages: ReadonlyArray<string>,
  readonly tags: ReadonlyArray<number>
} = {} as Schema.Schema.Encoded<typeof schemaLearningSettingsResponseDto>;
void _check03
const _check04: Schema.Schema.Encoded<typeof schemaLearningSettingsResponseDto> = {} as {
  readonly learnLanguages: ReadonlyArray<string>,
  readonly knownLanguages: ReadonlyArray<string>,
  readonly tags: ReadonlyArray<number>
};
void _check04

void (transform satisfies Schema.Schema<{
  readonly learnLangCodes: ReadonlyArray<LangCode>,
  readonly knownLangCodes: ReadonlyArray<LangCode>,
  readonly tagIds: ReadonlyArray<TagId>
}, {
  readonly learnLanguages: ReadonlyArray<string>,
  readonly knownLanguages: ReadonlyArray<string>,
  readonly tags: ReadonlyArray<number>
}>)
void ({} as {
  readonly learnLangCodes: ReadonlyArray<LangCode>,
  readonly knownLangCodes: ReadonlyArray<LangCode>,
  readonly tagIds: ReadonlyArray<TagId>
} satisfies Schema.Schema.Type<typeof transform>)
void ({} as Schema.Schema.Type<typeof transform> satisfies {
  readonly learnLangCodes: ReadonlyArray<LangCode>,
  readonly knownLangCodes: ReadonlyArray<LangCode>,
  readonly tagIds: ReadonlyArray<TagId>
})
void ({} as {
  readonly learnLanguages: ReadonlyArray<string>,
  readonly knownLanguages: ReadonlyArray<string>,
  readonly tags: ReadonlyArray<number>
} satisfies Schema.Schema.Encoded<typeof schemaLearningSettingsResponseDto>)
void ({} as Schema.Schema.Encoded<typeof schemaLearningSettingsResponseDto> satisfies {
  readonly learnLanguages: ReadonlyArray<string>,
  readonly knownLanguages: ReadonlyArray<string>,
  readonly tags: ReadonlyArray<number>
})

export const decodeLearningSettingsResponse = Schema.decode(transform);
