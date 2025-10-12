import {Effect, Stream} from 'effect';
import {
  type LearningSettingsUseCases,
  LearningSettingsUseCasesTag
} from '../../../../app-ports/LearningSettingsUseCases.ts';
import {useCallback, useEffect, useMemo, useState} from 'react';
import {type AsyncData, LoadingData, matchAsyncData, SuccessData} from '../../../../app-ports/types.ts';
import {LearningSettings} from '../../../../domain/LearningSettings.ts';
import {forkAppEffect, interruptFiber, promiseAppEffect} from '../../../../app/effect-runtime.ts';
import type {LangCode} from '../../../../domain/LangCode.ts';
import type {AppError} from '../../../../app-ports/errors/AppError.ts';

import type {TagId} from '../../../../domain/Tag.ts';
import type {Identifier} from '../../../../domain/identity/Identifier.ts';
import type {TagSmall} from '../../../../domain/TagSmall.ts';
import loglevel from 'loglevel';

const log = loglevel.getLogger('useLearningSettings')
log.setLevel(loglevel.levels.INFO)

const withUseCases = (f: (useCases: LearningSettingsUseCases) => Effect.Effect<LearningSettings, AppError>) =>
  LearningSettingsUseCasesTag.pipe(Effect.flatMap(f))

const refreshLearningSettings = withUseCases(useCases => useCases.refreshLearningSettings());

const useLearningSettings = () => {
  const [learningSettings, setLearningSettings] = useState<AsyncData<LearningSettings, AppError>>(LoadingData())

  useEffect(() => {
    const fiber = forkAppEffect(Effect.gen(function* () {
      const uc = yield* LearningSettingsUseCasesTag
      setLearningSettings(yield* uc.lastLearningSettings)
      yield* uc.streamLearningSettings.pipe(
        Stream.runForEach((data) => Effect.sync(() => setLearningSettings(data)))
      )
    }))
    promiseAppEffect(refreshLearningSettings).catch(e => log.error('Error refreshing learning settings', e))
    return () => {
      interruptFiber(fiber)
    }
  }, []);

  const runWithLearningSettings = useCallback((f: (useCases: LearningSettingsUseCases) => Effect.Effect<LearningSettings, AppError>) => {
    // TODO: should exit if already loading?

    // Keep previous settings while loading new ones
    const prevLearningSettings = learningSettings
    // change state to loading, keeping previous data if any
    setLearningSettings(matchAsyncData(learningSettings,
      (previous) => LoadingData(previous),
      () => LoadingData(LearningSettings.empty),
      (data) => LoadingData(data)))

    // Run the effect
    return promiseAppEffect(withUseCases(f)).then(
      // on success, update the state with new data
      (ls) => setLearningSettings(SuccessData(ls)),
      // on error, log it and restore previous state
      // TODO: should be always Success in order to cancel loading state?
      (error) => {
        log.error('Error loading learning settings', error)
        setLearningSettings(prevLearningSettings)
      }
    )
  }, [learningSettings])

  const addLearnLang = useCallback((langCode: LangCode) =>
      runWithLearningSettings(useCases => useCases.addLearnLang(langCode)),
    [runWithLearningSettings])

  const removeLearnLang = useCallback((langCode: LangCode) =>
      runWithLearningSettings(useCases => useCases.removeLearnLang(langCode)),
    [runWithLearningSettings])

  const addKnownLang = useCallback((langCode: LangCode) =>
      runWithLearningSettings(useCases => useCases.addKnownLang(langCode)),
    [runWithLearningSettings])

  const removeKnownLang = useCallback((langCode: LangCode) =>
      runWithLearningSettings(useCases => useCases.removeKnownLang(langCode)),
    [runWithLearningSettings])

  const addTag = useCallback((tag: { label: string }) =>
      runWithLearningSettings(useCases => useCases.addTag(tag)),
    [runWithLearningSettings])

  const removeTag = useCallback((tagId: Identifier<TagSmall>) =>
      runWithLearningSettings(useCases => useCases.removeTag(tagId as TagId)),
    [runWithLearningSettings])

  return useMemo(() => ({
    learningSettings,
    addLearnLang,
    removeLearnLang,
    addKnownLang,
    removeKnownLang,
    addTag,
    removeTag
  }), [learningSettings, addLearnLang, removeLearnLang, addKnownLang, removeKnownLang, addTag, removeTag])

}

export default useLearningSettings
