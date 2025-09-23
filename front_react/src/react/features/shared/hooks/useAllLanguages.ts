import type {Lang} from '../../../../domain/Lang.ts';
import {Effect} from 'effect';
import {useState} from 'react';
import {promiseAppEffect} from '../../../../app/effect-runtime.ts';
import {GetAllLanguagesUseCaseTag} from '../../../../app-ports/languages/GetAllLanguagesUseCase.ts';

const program: Effect.Effect<ReadonlyArray<Lang>, never, GetAllLanguagesUseCaseTag> = Effect.gen(function* () {
  const useCase = yield* GetAllLanguagesUseCaseTag
  return useCase.allLanguages
})


const useAllLanguages = (): ReadonlyArray<Lang> => {
  const [languages, setLanguages] = useState<ReadonlyArray<Lang>>([])
  promiseAppEffect(program).then(languages => setLanguages(languages))
  return languages
}

export default useAllLanguages
