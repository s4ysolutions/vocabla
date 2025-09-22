import {Context, Effect} from 'effect';
import type {Lang} from '../domain/Lang.ts';
import type {InfraError} from './infraError.ts';

export interface LangRepository {
  getAllLangs: () => Effect.Effect<{
    readonly defaultLang: Lang,
    readonly unknownLang: Lang,
    languages: ReadonlyArray<Lang>
  }, InfraError>
}

export class LangRepositoryTag extends Context.Tag<'LangRepository'>('LangRepository')<
  LangRepositoryTag,
  LangRepository
>() {
}
