import {Array, Effect, PubSub, Ref, Stream} from 'effect';
import type {LearningSettings} from '../domain/LearningSettings.ts';
import {type AsyncData, SuccessData} from '../app-ports/types.ts';
import type {LearningSettingsUseCases} from '../app-ports/LearningSettingsUseCases.ts';
import {Tag} from '../domain/Tag.ts';
import type {Lang} from '../domain/Lang.ts';

const emptySettings: LearningSettings = {
  learnLangs: Array.empty<Lang>(),
  knownLangs: Array.empty<Lang>(),
  tags: Array.empty<Tag>(),
}

const makeLearningSettingsUseCases = (): Effect.Effect<LearningSettingsUseCases> =>
  Effect.gen(function* () {
    const ref = yield* Ref.make<AsyncData<LearningSettings>>({
      _state: 'loading'
    })
    const hub: PubSub.PubSub<AsyncData<LearningSettings>> = yield* PubSub.sliding<AsyncData<LearningSettings>>(1)
    const stream: Stream.Stream<AsyncData<LearningSettings>> = Stream.fromPubSub(hub).pipe(
      Stream.tap((data) => Ref.set(ref, data)),
    )
    return {
      lastLearningSettings: ref.get,
      streamLearningSettings: stream,
      refreshLearningSettings: (studentId) => Effect.succeed(emptySettings).pipe(
        Effect.tap((data) => PubSub.publish(hub, SuccessData(data))),
      ),
      addLearnLang: (studentId, langCode) => Effect.succeed(undefined),
      removeLearnLang: (studentId, langCode) => Effect.succeed(undefined),
      addKnownLang: (studentId, langCode) => Effect.succeed(undefined),
      removeKnownLang: (studentId, langCode) => Effect.succeed(undefined),
      addTag: (studentId, tagName) => Effect.succeed(undefined),
      removeTag: (studentId, tagName) => Effect.succeed(undefined),
    }
  })

export default makeLearningSettingsUseCases;
