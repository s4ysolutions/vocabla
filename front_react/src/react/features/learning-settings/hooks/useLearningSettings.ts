import {Effect, Stream} from 'effect';
import {
  type LearningSettingsUseCases,
  LearningSettingsUseCasesTag
} from '../../../../app-ports/me/LearningSettingsUseCases.ts';
import {useCallback, useEffect, useMemo, useState} from 'react';
import {type AsyncData, LoadingData, matchAsyncData, SuccessData} from '../../../../app-ports/types.ts';
import {LearningSettings} from '../../../../domain/LearningSettings.ts';
import {forkAppEffect, interruptFiber, promiseAppEffect} from '../../../../app/effect-runtime.ts';
import type {LangCode} from '../../../../domain/LangCode.ts';
import type {AppError} from '../../../../app-ports/errors/AppError.ts';

import log from 'loglevel'
import type {TagId} from '../../../../domain/Tag.ts';
import type {Identifier} from '../../../../domain/identity/Identifier.ts';
import type {TagSmall} from '../../../../domain/TagSmall.ts';

log.getLogger('useLearningSettings').setLevel('debug')

const effect = (f: (useCases: LearningSettingsUseCases) => Effect.Effect<LearningSettings, AppError>) =>
  promiseAppEffect(LearningSettingsUseCasesTag.pipe(Effect.flatMap(f)))


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
    return () => {
      interruptFiber(fiber)
    }
  }, []);

  const withLearningSettings = useCallback((f: (useCases: LearningSettingsUseCases) => Effect.Effect<LearningSettings, AppError>) => {
    const prevLearningSettings = learningSettings
    log.debug('Loading learning settings with previous', prevLearningSettings)
    setLearningSettings(matchAsyncData(learningSettings,
      (previous) => LoadingData(previous),
      () => LoadingData(LearningSettings.empty),
      (data) => LoadingData(data)))
    return effect(f).then(
      (ls) => setLearningSettings(SuccessData(ls)),
      (error) => {
        log.error('Error loading learning settings', error)
        setLearningSettings(prevLearningSettings)
      }
    )
  }, [learningSettings])

  const addLearnLang = useCallback((langCode: LangCode) =>
      withLearningSettings(useCases => useCases.addLearnLang(langCode)),
    [withLearningSettings])

  const removeLearnLang = useCallback((langCode: LangCode) =>
      withLearningSettings(useCases => useCases.removeLearnLang(langCode)),
    [withLearningSettings])

  const addKnownLang = useCallback((langCode: LangCode) =>
      withLearningSettings(useCases => useCases.addKnownLang(langCode)),
    [withLearningSettings])

  const removeKnownLang = useCallback((langCode: LangCode) =>
      withLearningSettings(useCases => useCases.removeKnownLang(langCode)),
    [withLearningSettings])

  const addTag = useCallback((tag: { label: string }) =>
      withLearningSettings(useCases => useCases.addTag(tag)),
    [withLearningSettings])

  const removeTag = useCallback((tagId: Identifier<TagSmall>) =>
      withLearningSettings(useCases => useCases.removeTag(tagId as TagId)),
    [withLearningSettings])

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
