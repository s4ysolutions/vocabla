import {type Effect} from 'effect/Effect';
import type {InfraError} from './infraError.ts';
import {Context} from 'effect';
import type {StudentId} from '../domain/Student.ts';
import type {LangCode} from '../domain/LangCode.ts';
import type {TagId, Tag} from '../domain/Tag.ts';
import type {TagSmall} from '../domain/TagSmall.ts';
import type {Identified} from '../domain/identity/Identified.ts';

export type LearningSettingsR = {
  readonly learnLangCodes: ReadonlyArray<LangCode>,
  readonly knownLangCodes: ReadonlyArray<LangCode>,
  readonly tags: ReadonlyArray<Identified<TagSmall>>
}

export interface LearningSettingsRepository {
  getLearningSettings: (studentId: StudentId) => Effect<LearningSettingsR, InfraError>
  addKnownLang: (studentId: StudentId, langCode: LangCode) => Effect<LearningSettingsR, InfraError>
  removeKnownLang: (studentId: StudentId, langCode: LangCode) => Effect<LearningSettingsR, InfraError>
  addLearnLang: (studentId: StudentId, langCode: LangCode) => Effect<LearningSettingsR, InfraError>
  removeLearnLang: (studentId: StudentId, langCode: LangCode) => Effect<LearningSettingsR, InfraError>
  createTag: (studentId: StudentId, tag: Tag) => Effect<LearningSettingsR, InfraError>
  deleteTag: (studentId: StudentId, tagId: TagId) => Effect<LearningSettingsR, InfraError>
}

export class LearningSettingsRepositoryTag extends Context.Tag('LearningSettingsRepository')<
  LearningSettingsRepositoryTag,
  LearningSettingsRepository
>() {
}
