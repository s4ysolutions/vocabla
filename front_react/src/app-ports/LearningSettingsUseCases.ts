import type {LearningSettings} from '../domain/LearningSettings.ts';
import {Context, Effect, Stream} from 'effect';
import type {AsyncData} from './types.ts';
import type {AppError} from './errors/AppError.ts';
import type {LangCode} from '../domain/LangCode.ts';
import type {TagId} from '../domain/Tag.ts';

export interface LearningSettingsUseCases {
  readonly lastLearningSettings: Effect.Effect<AsyncData<LearningSettings, AppError>>
  readonly streamLearningSettings: Stream.Stream<AsyncData<LearningSettings, AppError>>

  refreshLearningSettings(): Effect.Effect<LearningSettings, AppError>

  addLearnLang(langCode: LangCode): Effect.Effect<LearningSettings, AppError>

  removeLearnLang(langCode: LangCode): Effect.Effect<LearningSettings, AppError>

  addKnownLang(langCode: LangCode): Effect.Effect<LearningSettings, AppError>

  removeKnownLang(langCode: LangCode): Effect.Effect<LearningSettings, AppError>

  addTag(tag: { label: string }): Effect.Effect<LearningSettings, AppError>

  removeTag(tagId: TagId): Effect.Effect<LearningSettings, AppError>
}

export class LearningSettingsUseCasesTag extends Context.Tag('LearningSettingsUseCasesTag')<
  LearningSettingsUseCasesTag,
  LearningSettingsUseCases>() {
}
