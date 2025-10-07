import type {Lang} from '../../../../domain/Lang.ts';
import {Effect} from 'effect';
import {useState} from 'react';
import {promiseAppEffect} from '../../../../app/effect-runtime.ts';
import {LanguagesUseCasesTag} from '../../../../app-ports/LanguagesUseCases.ts';
import type {AppError} from '../../../../app-ports/errors/AppError.ts';

const program: Effect.Effect<Lang, AppError, LanguagesUseCasesTag> =
  Effect.flatMap(LanguagesUseCasesTag, useCase => useCase.unknownLang)


const useUnknownLanguage = (): Lang | undefined => {
  const [lang, setLang] = useState<Lang | undefined>(undefined)
  promiseAppEffect(program).then(unknownLang => setLang(unknownLang))
  return lang
}

export default useUnknownLanguage;
