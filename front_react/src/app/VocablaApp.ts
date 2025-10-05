import {appError, type AppError} from '../app-ports/errors/AppError.ts';
import {Context, Effect, Layer} from 'effect';
import type {InfraError} from '../app-repo/InfraError.ts';
import {
  type GetEntriesByOwnerRequest,
  type GetEntriesByOwnerResponse,
  type GetEntriesByOwnerUseCase, GetEntriesByOwnerUseCaseTag
} from '../app-ports/entries/GetEntriesByOwner.ts';
import {type EntriesRepository, EntriesRepositoryTag} from '../app-repo/EntriesRepository.ts';
import {repositoryRestLive} from '../infra/repo/repositoryRestLive.ts';
import {
  type CreateEntryRequest,
  type CreateEntryResponse,
  type CreateEntryUseCase,
  CreateEntryUseCaseTag
} from '../app-ports/entries/CreateEntryUseCase.ts';
import {LangRepositoryTag} from '../app-repo/LangRepository.ts';
import type {LangCode} from '../domain/LangCode.ts';
import {type GetLangByCodeUseCase, GetLangByCodeUseCaseTag} from '../app-ports/languages/GetLangByCodeUseCase.ts';
import {type GetDefaultLangUseCase, GetDefaultLangUseCaseTag} from '../app-ports/languages/GetDefaultLangUseCase.ts';
import {defaultFallbackLang, type Lang, unknownFallbackLang} from '../domain/Lang.ts';
import {type GetAllLanguagesUseCase, GetAllLanguagesUseCaseTag} from '../app-ports/languages/GetAllLanguagesUseCase.ts';
import {LearningSettingsUseCasesTag} from '../app-ports/me/LearningSettingsUseCases.ts';
import {
  LearningSettingsRepositoryTag
} from '../app-repo/LearningSettingsRepository.ts';
import makeMeUseCases from './MeUseCasesLive.ts';
import {LearningSettingsUseCasesLive} from './LearningSettingsUseCasesLive.ts';


import log from 'loglevel'
log.getLogger('vocablaApp').setLevel('debug')

const vocablaApp = (entriesRepository: EntriesRepository, defaultLang: Lang, unknownLang: Lang, langByCode: Record<string, Lang>):
  GetLangByCodeUseCase &
  GetDefaultLangUseCase &
  GetAllLanguagesUseCase &
  CreateEntryUseCase &
  GetEntriesByOwnerUseCase => ({
  createEntry: (request: CreateEntryRequest): Effect.Effect<CreateEntryResponse, AppError> =>
    entriesRepository.createEntry(request.entry, request.tagIds).pipe(
      Effect.map(id => ({id})),
      Effect.mapError(_infra2appError)
    ),
  getEntriesByOwner: (request: GetEntriesByOwnerRequest): Effect.Effect<GetEntriesByOwnerResponse, AppError> =>
    entriesRepository.getEntriesByOwner(request.ownerId, request.filter).pipe(
      Effect.mapError(_infra2appError)
    ),
  getLangByCode: (code: LangCode): Lang => langByCode[code] ?? unknownLang,
  defaultLang,
  allLanguages: Object.values(langByCode)
});

const _infra2appError = (error: InfraError): AppError =>
  appError(error.message)

export type UseCases =
  | GetEntriesByOwnerUseCaseTag
  | CreateEntryUseCaseTag
  | GetLangByCodeUseCaseTag
  | GetDefaultLangUseCaseTag
  | GetAllLanguagesUseCaseTag
  | LearningSettingsUseCasesTag

export const vocablaAppLayer: Layer.Layer<
  UseCases,
  never,
  EntriesRepositoryTag | LangRepositoryTag | LearningSettingsRepositoryTag> =
  // I need effectContext to run effects to create the layer with many dependencies
  Layer.effectContext(
    // I need to use generators to get dependencies as effects
    Effect.gen(function* () {
      const entriesRepository = yield* EntriesRepositoryTag
      const langRepository = yield* LangRepositoryTag
      const learningSettingsRepository = yield* LearningSettingsRepositoryTag

      // create a singleton with all languages assuming they don't change often
      const l = yield* langRepository.getAllLangs().pipe(
        Effect.map(response => ({
          ...response,
          langsByCode: response.languages.reduce((acc, lang) => {
            acc[lang.code as string] = lang
            return acc
          }, {} as Record<string, Lang>)
        })),
        Effect.tapError(error => Effect.log('Error fetching languages: ' + error.message)),
        Effect.catchAll(() => Effect.succeed({
          langs: [defaultFallbackLang],
          langsByCode: {[defaultFallbackLang.code]: defaultFallbackLang},
          defaultLang: defaultFallbackLang,
          unknownLang: unknownFallbackLang,
        }))
      )

      // function to get lang by code or return unknownLang if not found
      const langByCode = (lang: LangCode): Lang => l.langsByCode[lang as string] ?? l.unknownLang

      // vocable app is depricated, but still used till the migration is complete
      const impl = vocablaApp(entriesRepository, l.defaultLang, l.unknownLang, l.langsByCode)
      // there will be services with few use cases grouped together to avoid too many context tags
      const meUseCases = new makeMeUseCases()
      const learningSettingsService = yield* LearningSettingsUseCasesLive.make(learningSettingsRepository, meUseCases, langByCode)

      log.debug('Refreshing learning settings at startup')
      yield *learningSettingsService.refreshLearningSettings()

      return Context.empty()
        .pipe(Context.add(GetEntriesByOwnerUseCaseTag, impl))
        .pipe(Context.add(CreateEntryUseCaseTag, impl))
        .pipe(Context.add(GetLangByCodeUseCaseTag, impl))
        .pipe(Context.add(GetDefaultLangUseCaseTag, impl))
        .pipe(Context.add(GetAllLanguagesUseCaseTag, impl))
        .pipe(Context.add(LearningSettingsUseCasesTag, learningSettingsService))
    })
  )

export const vocablaAppLive: Layer.Layer<UseCases> = vocablaAppLayer.pipe(
  Layer.provide(repositoryRestLive),
)
