import {
  type CreateTagRequest,
  type CreateTagResponse,
  type CreateTagUseCase,
  CreateTagUseCaseTag
} from '../app-ports/tags/CreateTagUseCase.ts';
import {type TagsRepository, TagsRepositoryTag} from '../app-repo/TagsRepository.ts';
import {appError, type AppError} from '../app-ports/errors/AppError.ts';
import {Context, Effect, Layer} from 'effect';
import type {InfraError} from '../app-repo/infraError.ts';
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
import type {LearningSettingsUseCases} from '../app-ports/me/LearningSettingsUseCases.ts';
import {
  type LearningSettingsRepository,
  LearningSettingsRepositoryTag
} from '../app-repo/LearningSettingsRepository.ts';
import makeLearningSettingsUseCases from './LearningSettingsService.ts';


const vocablaApp = (tagsRepository: TagsRepository, entriesRepository: EntriesRepository, defaultLang: Lang, unknownLang: Lang, langByCode: Record<LangCode, Lang>):
  GetLangByCodeUseCase &
  GetDefaultLangUseCase &
  GetAllLanguagesUseCase &
  CreateTagUseCase &
  CreateEntryUseCase &
  GetEntriesByOwnerUseCase => ({
  createTag: (request: CreateTagRequest): Effect.Effect<CreateTagResponse, AppError> =>
    tagsRepository.createTag(request.tag).pipe(
      Effect.map(id => ({id})),
      Effect.mapError(_infra2appError),
    ),
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
  CreateTagUseCaseTag
  | GetEntriesByOwnerUseCaseTag
  | CreateEntryUseCaseTag
  | GetLangByCodeUseCaseTag
  | GetDefaultLangUseCaseTag
  | GetAllLanguagesUseCaseTag
  | LearningSettingsUseCases

export const vocablaAppLayer: Layer.Layer<
  UseCases,
  never,
  TagsRepositoryTag | EntriesRepositoryTag | LangRepositoryTag | LearningSettingsRepositoryTag> =
  Layer.effectContext(
    Effect.gen(function* () {
      const entriesRepository = yield* EntriesRepositoryTag
      const tagsRepository = yield* TagsRepositoryTag
      const langRepository = yield* LangRepositoryTag
      const learningSettingsRepository = yield* LearningSettingsRepositoryTag

      const l = yield* langRepository.getAllLangs().pipe(
        Effect.map(response => ({
          ...response,
          langsByCode: response.languages.reduce((acc, lang) => {
            acc[lang.code] = lang
            return acc
          }, {} as Record<LangCode, Lang>)
        })),
        Effect.tapError(error => Effect.log('Error fetching languages: ' + error.message)),
        Effect.catchAll(() => Effect.succeed({
          langs: [defaultFallbackLang],
          langsByCode: {[defaultFallbackLang.code]: defaultFallbackLang},
          defaultLang: defaultFallbackLang,
          unknownLang: unknownFallbackLang,
        }))
      )

      const impl = vocablaApp(tagsRepository, entriesRepository, l.defaultLang, l.unknownLang, l.langsByCode)
      const learningSettingsService = makeLearningSettingsUseCases(learningSettingsRepository, l.langsByCode)

      return Context.empty()
        .pipe(Context.add(CreateTagUseCaseTag, impl))
        .pipe(Context.add(GetEntriesByOwnerUseCaseTag, impl))
        .pipe(Context.add(CreateEntryUseCaseTag, impl))
        .pipe(Context.add(GetLangByCodeUseCaseTag, impl))
        .pipe(Context.add(GetDefaultLangUseCaseTag, impl))
        .pipe(Context.add(GetAllLanguagesUseCaseTag, impl))
    })
  )

export const vocablaAppLive: Layer.Layer<UseCases> = vocablaAppLayer.pipe(
  Layer.provide(repositoryRestLive),
)
