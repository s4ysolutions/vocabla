import type {Lang} from '../../../../domain/Lang.ts';
import {Effect} from 'effect';
import {GetDefaultLangUseCaseTag} from '../../../../app-ports/languages/GetDefaultLangUseCase.ts';
import {useState} from 'react';
import {promiseAppEffect} from '../../../../app/effect-runtime.ts';

const program: Effect.Effect<Lang, never, GetDefaultLangUseCaseTag> = Effect.gen(function* () {
  const useCase = yield* GetDefaultLangUseCaseTag
  return useCase.defaultLang
})


const useDefaultLanguage = (): Lang | undefined => {
  const [lang, setLang] = useState<Lang | undefined>(undefined)
  promiseAppEffect(program).then(defaultLang => setLang(defaultLang))
  return lang
}

export default useDefaultLanguage;
