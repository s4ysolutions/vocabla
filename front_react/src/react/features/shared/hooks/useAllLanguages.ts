import type {Lang} from '../../../../domain/Lang.ts';
import {Effect} from 'effect';
import {useState} from 'react';
import {promiseAppEffect} from '../../../../app/effect-runtime.ts';
import {LanguagesUseCasesTag} from '../../../../app-ports/LanguagesUseCases.ts';
import type {AppError} from '../../../../app-ports/errors/AppError.ts';

const program: Effect.Effect<ReadonlyArray<Lang>, AppError, LanguagesUseCasesTag> =
  Effect.flatMap(LanguagesUseCasesTag,useCases => useCases.allLanguages)


const useAllLanguages = (): ReadonlyArray<Lang> => {
  const [languages, setLanguages] = useState<ReadonlyArray<Lang>>([])
  promiseAppEffect(program).then(languages => setLanguages(languages))
  return languages
}

export default useAllLanguages
