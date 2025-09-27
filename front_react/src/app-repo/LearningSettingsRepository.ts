import type {Effect} from 'effect/Effect';
import type {InfraError} from './infraError.ts';
import {Context} from 'effect';
import type {StudentId} from '../domain/Student.ts';
import type {LangCode} from '../domain/LangCode.ts';
import type {TagId} from '../domain/Tag.ts';

export interface LearningSettingsRepository {
  getLearningSettings: (studentId: StudentId) => Effect<{
    readonly learnLangCodes: ReadonlyArray<LangCode>,
    readonly knownLangCodes: ReadonlyArray<LangCode>,
    readonly tagIds: ReadonlyArray<TagId>
  }, InfraError>
}

export class LearningSettingsRepositoryTag extends Context.Tag('LearningSettingsRepository')<
  LearningSettingsRepositoryTag,
  LearningSettingsRepository
>() {
}
