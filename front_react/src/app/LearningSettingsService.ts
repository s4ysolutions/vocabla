import {Array, Effect, Option, PubSub, Ref, Stream} from 'effect';
import type {LearningSettings} from '../domain/LearningSettings.ts';
import {type AsyncData, SuccessData} from '../app-ports/types.ts';
import type {LearningSettingsUseCases} from '../app-ports/LearningSettingsUseCases.ts';
import {Tag, type TagId} from '../domain/Tag.ts';
import type {Lang} from '../domain/Lang.ts';
import {type LearningSettingsR, type LearningSettingsRepository} from '../app-repo/LearningSettingsRepository.ts';
import type {LangCode} from '../domain/LangCode.ts';
import type {InfraError} from '../app-repo/infraError.ts';
import {appError, type AppError} from '../app-ports/errors/AppError.ts';
import type {StudentId} from '../domain/Student.ts';

const makeLearningSettingsUseCases = (
  repository: LearningSettingsRepository,
  langByCode: (code: LangCode) => Lang):
  Effect.Effect<LearningSettingsUseCases> => Effect.gen(function* () {

  const ref = yield* Ref.make<AsyncData<LearningSettings>>({
    _state: 'loading'
  })
  const hub: PubSub.PubSub<AsyncData<LearningSettings>> = yield* PubSub.sliding<AsyncData<LearningSettings>>(1)
  const stream: Stream.Stream<AsyncData<LearningSettings>> = Stream.fromPubSub(hub).pipe(
    Stream.tap((data) => Ref.set(ref, data)),
  )
  const tagById = (id: TagId): Effect.Effect<Option.Option<Tag>> => Ref.get(ref).pipe(
    Effect.map(data => {
        switch (data._state) {
          case 'loading':
          case 'error':
            return Option.none()
          case 'success':
            return Array.findFirst(data.data.tags, (t) => t.id === id)
        }
      }
    )
  )

  const process = (effect: Effect.Effect<LearningSettingsR, InfraError>) =>
    effect.pipe(
      Effect.tap(() => Ref.set(ref, {_state: 'loading'})),
      Effect.map((data: LearningSettingsR) => ({
        learnLangs: data.learnLangCodes.map(langByCode),
        knownLangs: data.knownLangCodes.map(langByCode),
        tags: Array.getSomes(data.tagIds.map(tagById))
      })),
      Effect.tap((data) => PubSub.publish(hub, SuccessData(data))),
      Effect.mapError(_infra2appError)
    );

  const processF =
    (f: (studentId: StudentId, langCode: LangCode) => Effect.Effect<LearningSettingsR, InfraError>) =>
      (studentId: StudentId, langCode: LangCode) =>
        process(f(studentId, langCode));

  return {
    lastLearningSettings: ref.get,
    streamLearningSettings: stream,
    refreshLearningSettings: (studentId) => process(repository.getLearningSettings(studentId)),
    addLearnLang: processF(repository.addLearnLang),
    removeLearnLang: processF(repository.removeLearnLang),
    addKnownLang: processF(repository.addKnownLang),
    removeKnownLang: processF(repository.removeKnownLang),
    addTag: (studentId, tagId) => process(repository.createTag(studentId, tagId)),
    removeTag: (studentId, tagId) => process(repository.deleteTag(studentId, tagId)),
  }
})

const _infra2appError = (error: InfraError): AppError =>
  appError(error.message)

export default makeLearningSettingsUseCases;
