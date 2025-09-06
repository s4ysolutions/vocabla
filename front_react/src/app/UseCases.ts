import {TagsRepository, TagsRepositoryTag} from '../app-repo/TagsRepository.ts';
import {
  type CreateTagRequest,
  type CreateTagResponse,
  CreateTagUseCase,
  CreateTagUseCaseTag
} from '../app-ports/CreateTagUseCase.ts';
import {Context, Effect, Layer} from 'effect';
import type {AppError} from '../app-ports/errors/AppError.ts';
import {tt} from '../translable/Translatable.ts';

class UseCases implements CreateTagUseCase {
  private readonly tagsRepository: TagsRepository;

  constructor(tagsRepository: TagsRepository) {
    this.tagsRepository = tagsRepository;
  }

  createTag(request: CreateTagRequest): Effect.Effect<CreateTagResponse, AppError> {
    return this.tagsRepository.createTag(request.tag, request.ownerId).pipe(
      Effect.mapError((error): AppError => ({message: tt`Some error ${error}`})),
    );
  }
}


export const useCasesServicesLive: Layer.Layer<CreateTagUseCaseTag, never, TagsRepositoryTag> = Layer.effect(
  CreateTagUseCase,
  Effect.gen(function* () {
      const tagsRepository = yield* TagsRepository;
      return {
        createTag: (request: CreateTagRequest) =>
          tagsRepository.createTag(request.tag, request.ownerId).pipe(
            Effect.mapError((error): AppError => ({message: tt`Some error`})),
          )
      }
    }
  )
);
