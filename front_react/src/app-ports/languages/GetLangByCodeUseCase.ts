import type {LangCode} from '../../domain/LangCode.ts';
import type {Lang} from '../../domain/Lang.ts';
import {Context} from 'effect';

export interface GetLangByCodeUseCase {
  getLangByCode(code: LangCode): Lang
}

export class GetLangByCodeUseCaseTag extends Context.Tag('GetLangByCodeUseCaseTag')<
  GetLangByCodeUseCaseTag,
  GetLangByCodeUseCase
>() {
}
