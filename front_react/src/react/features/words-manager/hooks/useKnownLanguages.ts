import {useEffect, useState} from 'react';
import {type AsyncData, isSuccess, matchAsyncData, SuccessData} from '../../../../app-ports/types.ts';
import {forkAppEffect, interruptFiber} from '../../../../app/effect-runtime.ts';
import {Effect, Stream} from 'effect';
import {LearningSettingsUseCasesTag} from '../../../../app-ports/LearningSettingsUseCases.ts';
import type {Lang} from '../../../../domain/Lang.ts';
import {learningSettings2knownLangs} from './mappers.ts';
import {LanguagesUseCasesTag} from '../../../../app-ports/LanguagesUseCases.ts';
import loglevel from 'loglevel';

const log = loglevel.getLogger('useKnownLanguages')
log.setLevel(loglevel.levels.DEBUG)

const useKnownLanguages = () => {

  const [knownLanguages, setLanguages] = useState<ReadonlyArray<Lang>>([])
  const [loading, setLoading] = useState(true)

  const setLS = (als: AsyncData<ReadonlyArray<Lang>>) =>
    matchAsyncData(als,
      (previous) => {
        setLoading(true)
        setLanguages(previous || [])
      },
      () => {
        setLoading(false)
        setLanguages([])
      },
      (langs) => {
        setLoading(false)
        setLanguages(langs)
      })

  useEffect(() => {
    // subscribe to updates
    const fiber = forkAppEffect(
      Effect.gen(function* () {
        // Get services
        const luc = yield* LanguagesUseCasesTag
        const suc = yield* LearningSettingsUseCasesTag

        // Get default language once
        const fallbackLang = yield* luc.defaultLang
        // And create a mapper with it
        const mapper = learningSettings2knownLangs(fallbackLang)

        // Set initial value
        const initialSettings = yield* suc.lastLearningSettings
        setLS(mapper(initialSettings))

        log.debug('Initial settings', initialSettings)

        if (!isSuccess(initialSettings)) {
          log.debug('Refreshed learning settings...')
          setLS(mapper(SuccessData(yield* suc.refreshLearningSettings())))
          log.debug('Refreshed learning settings')
        }

        log.debug('Subscribing to learning settings changes...')
        // Subscribe to updates
        yield* suc.streamLearningSettings.pipe(
          Stream.runForEach((ls) =>
            Effect.sync(() => setLS(mapper(ls)))
          )
        )
      })
    )
    return () => interruptFiber(fiber)
  }, [])

  return {knownLanguages, loading}
}

export default useKnownLanguages
