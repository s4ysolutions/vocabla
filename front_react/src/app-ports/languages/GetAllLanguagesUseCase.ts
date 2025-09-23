import type {Lang} from '../../domain/Lang.ts';
import {Context} from 'effect';

export interface GetAllLanguagesUseCase {
  readonly allLanguages: ReadonlyArray<Lang>
}

export class GetAllLanguagesUseCaseTag extends Context.Tag('GetAllLanguagesUseCaseTag')<
  GetAllLanguagesUseCaseTag,
  GetAllLanguagesUseCase
>() {
}
