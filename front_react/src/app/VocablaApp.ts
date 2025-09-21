import {
  type CreateTagRequest,
  type CreateTagResponse,
  type CreateTagUseCase,
  CreateTagUseCaseTag
} from '../app-ports/CreateTagUseCase.ts';
import {type TagsRepository, TagsRepositoryTag} from '../app-repo/TagsRepository.ts';
import {appError, type AppError} from '../app-ports/errors/AppError.ts';
import {Context, Effect, Layer} from 'effect';
import type {InfraError} from '../app-repo/infraError.ts';
import {
  type GetEntriesByOwnerRequest,
  type GetEntriesByOwnerResponse,
  type GetEntriesByOwnerUseCase, GetEntriesByOwnerUseCaseTag
} from '../app-ports/GetEntriesByOwner.ts';
import {type EntriesRepository, EntriesRepositoryTag} from '../app-repo/EntriesRepository.ts';
import {repositoryRestLive} from '../infra/repo/repositoryRestLive.ts';
import {
  type CreateEntryRequest,
  type CreateEntryResponse,
  type CreateEntryUseCase,
  CreateEntryUseCaseTag
} from '../app-ports/CreateEntryUseCase.ts';

const vocablaApp = (tagsRepository: TagsRepository, entriesRepository: EntriesRepository):
  CreateTagUseCase &
  CreateEntryUseCase &
  GetEntriesByOwnerUseCase =>
  ({
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
      )
  })

const _infra2appError = (error: InfraError): AppError =>
  appError(error.message)

export type UseCases = CreateTagUseCaseTag | GetEntriesByOwnerUseCaseTag | CreateEntryUseCaseTag

export const vocablaAppLayer: Layer.Layer<UseCases, never, TagsRepositoryTag | EntriesRepositoryTag> =
  Layer.effectContext(
    Effect.gen(function* () {
      const entriesRepository = yield* EntriesRepositoryTag
      const tagsRepository = yield* TagsRepositoryTag
      const impl = vocablaApp(tagsRepository, entriesRepository)

      return Context.empty()
        .pipe(Context.add(CreateTagUseCaseTag, impl))
        .pipe(Context.add(GetEntriesByOwnerUseCaseTag, impl))
        .pipe(Context.add(CreateEntryUseCaseTag, impl))
    })
  )

export const vocablaAppLive: Layer.Layer<UseCases> = vocablaAppLayer.pipe(
  Layer.provide(repositoryRestLive),
)
