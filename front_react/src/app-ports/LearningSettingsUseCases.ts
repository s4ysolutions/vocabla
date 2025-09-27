import type {LearningSettings} from '../domain/LearningSettings.ts';
import {Context, Effect, Stream} from 'effect';
import type {AsyncData} from './types.ts';
import type {StudentId} from '../domain/Student.ts';

export interface LearningSettingsUseCases {
  readonly lastLearningSettings: Effect.Effect<AsyncData<LearningSettings>>
  readonly streamLearningSettings: Stream.Stream<AsyncData<LearningSettings>>

  refreshLearningSettings(studentId: StudentId): Effect.Effect<LearningSettings>

  addLearnLang(studentId: StudentId, langCode: string): Effect.Effect<void>

  removeLearnLang(studentId: StudentId, langCode: string): Effect.Effect<void>

  addKnownLang(studentId: StudentId, langCode: string): Effect.Effect<void>

  removeKnownLang(studentId: StudentId, langCode: string): Effect.Effect<void>

  addTag(studentId: StudentId, tagName: string): Effect.Effect<void>

  removeTag(studentId: StudentId, tagName: string): Effect.Effect<void>
}

export class LearningSettingsUseCasesTag extends Context.Tag('LearningSettingsUseCasesTag')<
  LearningSettingsUseCasesTag,
  LearningSettingsUseCases>() {
}
