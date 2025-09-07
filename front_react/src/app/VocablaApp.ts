import {
  type CreateTagRequest,
  type CreateTagResponse,
  type CreateTagUseCase,
  CreateTagUseCaseTag
} from '../app-ports/CreateTagUseCase.ts';
import {type TagsRepository, TagsRepositoryTag} from '../app-repo/TagsRepository.ts';
import {appError, type AppError} from '../app-ports/errors/AppError.ts';
import {Effect, Layer} from 'effect';
import type {InfraError} from '../app-repo/infraError.ts';

const vocablaApp = (tagsRepository: TagsRepository): CreateTagUseCase => ({
  createTag: (request: CreateTagRequest): Effect.Effect<CreateTagResponse, AppError> =>
    Effect.mapError(tagsRepository.createTag(request.tag), _infra2appError)
})

const _infra2appError = (error: InfraError): AppError =>
  appError(error.message)

export const VocablaAppLive: Layer.Layer<CreateTagUseCaseTag, never, TagsRepositoryTag> = Layer.effect(
  CreateTagUseCaseTag,
  Effect.map(TagsRepositoryTag, vocablaApp)
);
