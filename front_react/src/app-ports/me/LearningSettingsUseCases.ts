import type {LearningSettings} from '../../domain/LearningSettings.ts';
import {Context, Effect, Stream} from 'effect';
import type {AsyncData} from '../types.ts';
import type {StudentId} from '../../domain/Student.ts';
import type {AppError} from '../errors/AppError.ts';
import type {LangCode} from '../../domain/LangCode.ts';
import type {TagId} from '../../domain/Tag.ts';

export interface LearningSettingsUseCases {
  readonly lastLearningSettings: Effect.Effect<AsyncData<LearningSettings>>
  readonly streamLearningSettings: Stream.Stream<AsyncData<LearningSettings>>

  refreshLearningSettings(studentId: StudentId): Effect.Effect<LearningSettings, AppError>

  addLearnLang(studentId: StudentId, langCode: LangCode): Effect.Effect<LearningSettings, AppError>

  removeLearnLang(studentId: StudentId, langCode: LangCode): Effect.Effect<LearningSettings, AppError>

  addKnownLang(studentId: StudentId, langCode: LangCode): Effect.Effect<LearningSettings, AppError>

  removeKnownLang(studentId: StudentId, langCode: LangCode): Effect.Effect<LearningSettings, AppError>

  addTag(studentId: StudentId, tagId: TagId): Effect.Effect<LearningSettings, AppError>

  removeTag(studentId: StudentId, tagId: TagId): Effect.Effect<LearningSettings, AppError>
}

export class LearningSettingsUseCasesTag extends Context.Tag('LearningSettingsUseCasesTag')<
  LearningSettingsUseCasesTag,
  LearningSettingsUseCases>() {
}
