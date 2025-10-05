import {Effect, Stream} from 'effect';
import {
  type LearningSettingsUseCases,
  LearningSettingsUseCasesTag
} from '../../../../app-ports/me/LearningSettingsUseCases.ts';
import {useEffect, useState} from 'react';
import {type AsyncData, LoadingData, SuccessData} from '../../../../app-ports/types.ts';
import type {LearningSettings} from '../../../../domain/LearningSettings.ts';
import {forkAppEffect, interruptFiber, promiseAppEffect} from '../../../../app/effect-runtime.ts';
import type {LangCode} from '../../../../domain/LangCode.ts';
import type {AppError} from '../../../../app-ports/errors/AppError.ts';

import log from 'loglevel'
import type {TagId} from '../../../../domain/Tag.ts';
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

  const withLearningSettings = (f: (useCases: LearningSettingsUseCases) => Effect.Effect<LearningSettings, AppError>) => {
    log.debug('Loading learning settings...')
    return effect(f).then(
      (ls) => setLearningSettings(SuccessData(ls)),
      (error) => log.error('Error loading learning settings', error)
    )
  }

  const addLearnLang = (langCode: LangCode) => withLearningSettings(useCases =>
    useCases.addLearnLang(langCode))

  const removeLearnLang = (langCode: LangCode) => withLearningSettings(useCases =>
    useCases.removeLearnLang(langCode))

  const addKnownLang = (langCode: LangCode) => withLearningSettings(useCases =>
    useCases.addKnownLang(langCode))

  const removeKnownLang = (langCode: LangCode) => withLearningSettings(useCases =>
    useCases.removeKnownLang(langCode))

  const addTag = (label: string) => withLearningSettings(useCases =>
    useCases.addTag({label}))

  const removeTag = (tagId: TagId) => withLearningSettings(useCases =>
    useCases.removeTag(tagId))

  return {
    learningSettings,
    addLearnLang,
    removeLearnLang,
    addKnownLang,
    removeKnownLang,
    addTag,
    removeTag
  }
}

export default useLearningSettings
