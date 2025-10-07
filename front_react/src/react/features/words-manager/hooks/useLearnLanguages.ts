import {useEffect, useState} from 'react';
import {type AsyncData, isSuccess, matchAsyncData} from '../../../../app-ports/types.ts';
import {forkAppEffect, interruptFiber} from '../../../../app/effect-runtime.ts';
import {Effect, Stream} from 'effect';
import {LearningSettingsUseCasesTag} from '../../../../app-ports/LearningSettingsUseCases.ts';
import type {Lang} from '../../../../domain/Lang.ts';
import {learningSettings2learnLangs} from './mappers.ts';
import {LanguagesUseCasesTag} from '../../../../app-ports/LanguagesUseCases.ts';
import loglevel from 'loglevel';

const log = loglevel.getLogger('useLearnLanguages')
log.setLevel(loglevel.levels.DEBUG)

const useLearnLanguages = () => {

  const [learnLanguages, setLanguages] = useState<ReadonlyArray<Lang>>([])
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
        log.debug('Learn languages updated', langs)
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
        const mapper = learningSettings2learnLangs(fallbackLang)

        // Set initial value
        const initialSettings = yield* suc.lastLearningSettings
        setLS(mapper(initialSettings))


        log.debug('Initial learning settings', initialSettings, isSuccess(initialSettings))
        if (!isSuccess(initialSettings)) {
          log.debug('Refreshed learning settings...')
          yield* suc.refreshLearningSettings()
          log.debug('Refreshed learning settings')
        }

        log.debug('Subscribing to learning settings changes...')
        // Subscribe to updates
        yield* suc.streamLearningSettings.pipe(
          Stream.runForEach((ls) => {
              log.debug('Learning settings changed', ls)
              return Effect.sync(() => setLS(mapper(ls)))
            }
          )
        )
      })
    )

    log.debug('Forked fiber to sync learning languages', fiber)
    return () => interruptFiber(fiber)
  }, [])

  return {learnLanguages, loading}
}

export default useLearnLanguages
