/* eslint-disable @typescript-eslint/no-this-alias */
import {Effect, Layer, PubSub, Ref, Stream} from 'effect';
import {LearningSettings} from '../domain/LearningSettings.ts';
import {type AsyncData, LoadingData, matchAsyncData, SuccessData} from '../app-ports/types.ts';
import {
  type LearningSettingsR,
  type LearningSettingsRepository,
  LearningSettingsRepositoryTag
} from '../app-repo/LearningSettingsRepository.ts';
import type {LangCode} from '../domain/LangCode.ts';
import type {InfraError} from '../app-repo/InfraError.ts';
import {type AppError} from '../app-ports/errors/AppError.ts';
import type {StudentId} from '../domain/Student.ts';
import {type LearningSettingsUseCases, LearningSettingsUseCasesTag} from '../app-ports/LearningSettingsUseCases.ts';
import {type MeUseCases, MeUseCasesTag} from '../app-ports/MeUseCases.ts';

import loglevel from 'loglevel'
import {type LanguagesUseCases, LanguagesUseCasesTag} from '../app-ports/LanguagesUseCases.ts';
import infra2appError from './infra2appError.ts';

const log = loglevel.getLogger('makeLearningSettingsUseCases')
log.setLevel(loglevel.levels.DEBUG)

/*
const df = (o: unknown): unknown => {
  log.debug('====>', o)
  return o
}
*/
class LearningSettingsUseCasesLive implements LearningSettingsUseCases {
  private constructor(
    private readonly refLastLearningSettings: Ref.Ref<AsyncData<LearningSettings, AppError>>,
    private readonly refLastStudentId: Ref.Ref<StudentId | null>,
    private readonly hub: PubSub.PubSub<AsyncData<LearningSettings, AppError>>,
    public readonly lastLearningSettings: Effect.Effect<AsyncData<LearningSettings, AppError>>,
    public readonly streamLearningSettings: Stream.Stream<AsyncData<LearningSettings, AppError>>,
    private readonly repository: LearningSettingsRepository,
    private readonly meUseCases: MeUseCases,
    private readonly languagesUseCases: LanguagesUseCases,
  ) {
  }

  static make(
    repository: LearningSettingsRepository,
    meUseCases: MeUseCases,
    languagesUseCases: LanguagesUseCases,
  ): Effect.Effect<LearningSettingsUseCasesLive> {
    return Effect.gen(function* () {
      const refLastSettings = yield* Ref.make(LoadingData<LearningSettings, AppError>())
      const refStudentId = yield* Ref.make<StudentId | null>(null)
      const hub = yield* PubSub.sliding<AsyncData<LearningSettings, AppError>>(1)
      const stream = Stream.fromPubSub(hub).pipe(Stream.tap((data) => Ref.set(refLastSettings, data)))

      return new LearningSettingsUseCasesLive(
        refLastSettings,
        refStudentId,
        hub,
        Ref.get(refLastSettings),
        stream,
        repository,
        meUseCases,
        languagesUseCases
      )
    })
  }

  static readonly layer: Layer.Layer<LearningSettingsUseCasesTag, never, LearningSettingsRepositoryTag | MeUseCasesTag | LanguagesUseCasesTag> = Layer.effect(
    LearningSettingsUseCasesTag,
    Effect.all({
      repository: LearningSettingsRepositoryTag,
      meUseCases: MeUseCasesTag,
      languagesUseCases: LanguagesUseCasesTag
    }).pipe(
      Effect.flatMap(({repository, meUseCases, languagesUseCases}) =>
        LearningSettingsUseCasesLive.make(repository, meUseCases, languagesUseCases)
      )
    )
  )

  private setState(data: AsyncData<LearningSettings, AppError>) {
    return Ref.set(this.refLastLearningSettings, data).pipe(
      Effect.flatMap(() => PubSub.publish(this.hub, data))
    )
  }

  process(effect: Effect.Effect<LearningSettingsR, InfraError>): Effect.Effect<LearningSettings, AppError> {
    const self = this
    return Effect.gen(function* () {
        const als = yield* self.lastLearningSettings
        if (als._state === 'success') {
          yield* self.setState(LoadingData(als.data))
        } else {
          yield* self.setState(LoadingData())
        }

        const learnSettingsR = yield* effect.pipe(Effect.mapError(infra2appError))
        const learnLangs =
          yield* Effect.all(learnSettingsR.learnLangCodes.map(code => self.languagesUseCases.getLangByCode(code)))
        const knownLangs =
          yield* Effect.all(learnSettingsR.knownLangCodes.map(code => self.languagesUseCases.getLangByCode(code)))
        const learningSettings: LearningSettings = {
          learnLangs,
          knownLangs,
          tags: learnSettingsR.tags
        }
        yield* self.setState(SuccessData(learningSettings))
        return learningSettings
      }
    )
  }

  private prevSettings(currentStudentId ?: StudentId):
    Effect.Effect<LearningSettings, AppError> {
    return Effect.gen(function* (this: LearningSettingsUseCasesLive) {
      const prevStudentId = yield* Ref.get(this.refLastStudentId)

      if (!currentStudentId || prevStudentId !== currentStudentId) {
        return LearningSettings.empty
      }

      const cachedData = yield* Ref.get(this.refLastLearningSettings)
      return cachedData._state === 'success'
        ? cachedData.data
        : LearningSettings.empty
    })
  }

  private withStudentId(f: (studentId: StudentId) => Effect.Effect<LearningSettings, AppError>):
    Effect.Effect<LearningSettings, AppError> {
    return this.meUseCases.currentStudentId.pipe(
      Effect.flatMap((lastStudentId) => matchAsyncData(lastStudentId,
          (previous) => this.prevSettings(previous),
          (error) => Effect.fail(error),
          (studentId) => f(studentId)
        )
      ),
    )
  }

  private processStudent(f: (studentId: StudentId) => Effect.Effect<LearningSettingsR, InfraError>): Effect.Effect<LearningSettings, AppError> {
    return this.withStudentId((studentId) => this.process(f(studentId)))
  }

  private processStudentAndLangCode(f: (studentId: StudentId, langCode: LangCode) => Effect.Effect<LearningSettingsR, InfraError>) {
    return (langCode: LangCode) =>
      this.withStudentId((studentId) => this.process(f(studentId, langCode)))
  }

  refreshLearningSettings(): Effect.Effect<LearningSettings, AppError> {
    return this.processStudent(this.repository.getLearningSettings.bind(this.repository))
  }

  addLearnLang(langCode: LangCode): Effect.Effect<LearningSettings, AppError> {
    return this.processStudentAndLangCode(this.repository.addLearnLang.bind(this.repository))(langCode)
  }

  removeLearnLang(langCode: LangCode): Effect.Effect<LearningSettings, AppError> {
    return this.processStudentAndLangCode(this.repository.removeLearnLang.bind(this.repository))(langCode)
  }

  addKnownLang(langCode: LangCode): Effect.Effect<LearningSettings, AppError> {
    return this.processStudentAndLangCode(this.repository.addKnownLang.bind(this.repository))(langCode)
  }

  removeKnownLang(langCode: LangCode): Effect.Effect<LearningSettings, AppError> {
    return this.processStudentAndLangCode(this.repository.removeKnownLang.bind(this.repository))(langCode)
  }

  addTag(tag: { label: string }): Effect.Effect<LearningSettings, AppError> {
    return this.processStudent(studentId => this.repository.createTag(studentId, tag))
  }

  removeTag(tagId: number): Effect.Effect<LearningSettings, AppError> {
    return this.processStudent(studentId => this.repository.deleteTag(studentId, tagId))
  }
}

export default LearningSettingsUseCasesLive
