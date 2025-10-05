import {Effect, PubSub, Ref, Stream} from 'effect';
import {LearningSettings} from '../domain/LearningSettings.ts';
import {type AsyncData, LoadingData, matchAsyncData, SuccessData} from '../app-ports/types.ts';
import type {Lang} from '../domain/Lang.ts';
import {type LearningSettingsR, type LearningSettingsRepository} from '../app-repo/LearningSettingsRepository.ts';
import type {LangCode} from '../domain/LangCode.ts';
import type {InfraError} from '../app-repo/InfraError.ts';
import {appError, type AppError} from '../app-ports/errors/AppError.ts';
import type {StudentId} from '../domain/Student.ts';
import type {LearningSettingsUseCases} from '../app-ports/me/LearningSettingsUseCases.ts';
import type {MeUseCases} from '../app-ports/me/MeUseCases.ts';

import log from 'loglevel'

log.getLogger('makeLearningSettingsUseCases').setLevel('debug')

export class LearningSettingsUseCasesLive implements LearningSettingsUseCases {
  private constructor(
    private readonly refLastLearningSettings: Ref.Ref<AsyncData<LearningSettings, AppError>>,
    private readonly refLastStudentId: Ref.Ref<StudentId | null>,
    private readonly hub: PubSub.PubSub<AsyncData<LearningSettings, AppError>>,
    public readonly lastLearningSettings: Effect.Effect<AsyncData<LearningSettings, AppError>>,
    public readonly streamLearningSettings: Stream.Stream<AsyncData<LearningSettings, AppError>>,
    private readonly repository: LearningSettingsRepository,
    private readonly meUseCases: MeUseCases,
    private readonly langByCode: (code: LangCode) => Lang
  ) {
  }

  static make(
    repository: LearningSettingsRepository,
    meUseCases: MeUseCases,
    langByCode: (code: LangCode) => Lang
  ): Effect.Effect<LearningSettingsUseCases> {
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
        langByCode
      )
    })
  }

  private setState(data: AsyncData<LearningSettings, AppError>) {
    return Ref.set(this.refLastLearningSettings, data).pipe(
      Effect.flatMap(() => PubSub.publish(this.hub, data))
    )
  }

  private process(effect: Effect.Effect<LearningSettingsR, InfraError>): Effect.Effect<LearningSettings, AppError> {
    return Effect.Do.pipe(
      //Effect.tap(() => this.setState(LoadingData())),
      Effect.flatMap(() => effect),
      Effect.map((data: LearningSettingsR): LearningSettings => ({
        learnLangs: data.learnLangCodes.map(this.langByCode),
        knownLangs: data.knownLangCodes.map(this.langByCode),
        tags: data.tags
      })),
      Effect.tap((data) => this.setState(SuccessData(data))),
      Effect.mapError(_infra2appError)
    )
  }

  private prevSettings(currentStudentId?: StudentId): Effect.Effect<LearningSettings, AppError> {
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

  private withStudentId(
    f: (studentId: StudentId) => Effect.Effect<LearningSettings, AppError>
  ): Effect.Effect<LearningSettings, AppError> {
    return this.meUseCases.currentStudentId.pipe(
      Effect.tap((studentId) => Effect.sync(() =>
        log.debug('LearningSettingsUseCasesLive.withStudentId:', studentId)
      )),
      Effect.flatMap((lastStudentId) =>
        matchAsyncData(lastStudentId,
          (previous) => this.prevSettings(previous),
          (error) => Effect.fail(error),
          (studentId) => f(studentId)
        )
      )
    )
  }

  private processStudent(f: (studentId: StudentId) => Effect.Effect<LearningSettingsR, InfraError>): Effect.Effect<LearningSettings, AppError> {
    return this.withStudentId((studentId) => this.process(f(studentId)))
  }

  private processStudentAndLangCode(
    f: (studentId: StudentId, langCode: LangCode) => Effect.Effect<LearningSettingsR, InfraError>
  ) {
    return (langCode: LangCode) =>
      this.withStudentId((studentId) => this.process(f(studentId, langCode)))
  }

  refreshLearningSettings(): Effect.Effect<LearningSettings, AppError> {
    return this.processStudent(this.repository.getLearningSettings)
  }

  addLearnLang(langCode: LangCode): Effect.Effect<LearningSettings, AppError> {
    return this.processStudentAndLangCode(this.repository.addLearnLang)(langCode)
  }

  removeLearnLang(langCode: LangCode): Effect.Effect<LearningSettings, AppError> {
    return this.processStudentAndLangCode(this.repository.removeLearnLang)(langCode)
  }

  addKnownLang(langCode: LangCode): Effect.Effect<LearningSettings, AppError> {
    return this.processStudentAndLangCode(this.repository.addKnownLang)(langCode)
  }

  removeKnownLang(langCode: LangCode): Effect.Effect<LearningSettings, AppError> {
    return this.processStudentAndLangCode(this.repository.removeKnownLang)(langCode)
  }

  addTag(tag: { label: string }): Effect.Effect<LearningSettings, AppError> {
    return this.processStudent(studentId => this.repository.createTag(studentId, tag))
  }

  removeTag(tagId: number): Effect.Effect<LearningSettings, AppError> {
    return this.processStudent(studentId => this.repository.deleteTag(studentId, tagId))
  }
}

const _infra2appError = (error: InfraError): AppError =>
  appError(error.message)
