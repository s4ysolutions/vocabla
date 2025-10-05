import {Effect, Match, PubSub, Ref, Stream} from 'effect';
import {LearningSettings} from '../domain/LearningSettings.ts';
import {type AsyncData, LoadingData, SuccessData} from '../app-ports/types.ts';
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
    private readonly ref: Ref.Ref<AsyncData<LearningSettings, AppError>>,
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
      const ref = yield* Ref.make(LoadingData<LearningSettings, AppError>())
      const hub = yield* PubSub.sliding<AsyncData<LearningSettings, AppError>>(1)
      const stream = Stream.fromPubSub(hub).pipe(Stream.tap((data) => Ref.set(ref, data)))

      return new LearningSettingsUseCasesLive(
        ref,
        hub,
        Ref.get(ref),
        stream,
        repository,
        meUseCases,
        langByCode
      )
    })
  }

  private setState(data: AsyncData<LearningSettings, AppError>) {
    return Ref.set(this.ref, data).pipe(
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

  private withLastStudentId<A>(
    f: (studentId: StudentId) => Effect.Effect<A, AppError>
  ): Effect.Effect<A, AppError> {
    const self = this
    return Effect.gen(function* () {
      const lastStudentId = yield* self.meUseCases.lastStudentId
      log.debug('LearningSettingsUseCasesLive.withLastStudentId: lastStudentId', lastStudentId)
      return yield* Match.value(lastStudentId).pipe(
        Match.when({_state: 'loading'}, () => Effect.succeed(LearningSettings.empty as A)),
        Match.when({_state: 'error'}, ({error}) => Effect.fail(error)),
        Match.when({_state: 'success'}, ({data}) => f(data)),
        Match.exhaustive
      )
    })
  }

  private processStudent(f: (studentId: StudentId) => Effect.Effect<LearningSettingsR, InfraError>): Effect.Effect<LearningSettings, AppError> {
    return this.withLastStudentId((studentId) => this.process(f(studentId)))
  }

  private processStudentAndLangCode(
    f: (studentId: StudentId, langCode: LangCode) => Effect.Effect<LearningSettingsR, InfraError>
  ) {
    return (langCode: LangCode) =>
      this.withLastStudentId((studentId) => this.process(f(studentId, langCode)))
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
