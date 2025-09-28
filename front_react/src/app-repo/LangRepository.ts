import {Context, Effect} from 'effect';
import type {Lang} from '../domain/Lang.ts';
import type {InfraError} from './infraError.ts';

export type AllLangsR = {
  readonly defaultLang: Lang,
  readonly unknownLang: Lang,
  languages: ReadonlyArray<Lang>
}
export interface LangRepository {
  getAllLangs: () => Effect.Effect<AllLangsR, InfraError>
}

export class LangRepositoryTag extends Context.Tag<'LangRepository'>('LangRepository')<
  LangRepositoryTag,
  LangRepository
>() {
}
