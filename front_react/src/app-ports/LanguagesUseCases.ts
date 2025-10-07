import type {Lang} from '../domain/Lang.ts';
import {Context, Effect} from 'effect';
import type {LangCode} from '../domain/LangCode.ts';
import type {AppError} from './errors/AppError.ts';

export interface LanguagesUseCases {
  readonly allLanguages: Effect.Effect<ReadonlyArray<Lang>, AppError>
  readonly defaultLang: Effect.Effect<Lang, AppError>
  readonly unknownLang: Effect.Effect<Lang, AppError>
  getLangByCode(code: LangCode): Effect.Effect<Lang, AppError>
}

export class LanguagesUseCasesTag extends Context.Tag('LanguagesUseCasesTag')<
  LanguagesUseCasesTag,
  LanguagesUseCases
>() {
}
