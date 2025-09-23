import type {Lang} from '../../domain/Lang.ts';
import {Context} from 'effect';

export interface GetDefaultLangUseCase {
  defaultLang: Lang
}

export class GetDefaultLangUseCaseTag extends Context.Tag('GetDefaultLangUseCaseTag')<
  GetDefaultLangUseCaseTag,
  GetDefaultLangUseCase
>() {
}
