import {Context, Effect, Layer} from 'effect';
import {RestClientTag} from '../rest/restClient.ts';
import {TagsRepositoryTag} from '../../app-repo/TagsRepository.ts';
import {EntriesRepositoryTag} from '../../app-repo/EntriesRepository.ts';
import {repositoryRest} from './repositoryRest.ts';

export const repositoryRestLayer: Layer.Layer<TagsRepositoryTag | EntriesRepositoryTag, never, RestClientTag> =
  Layer.effectContext(
    Effect.gen(function* () {
      const restClient = yield* RestClientTag
      const repositoryImpl = repositoryRest(restClient) // Single instance

      return Context.empty()
        .pipe(Context.add(TagsRepositoryTag, repositoryImpl))
        .pipe(Context.add(EntriesRepositoryTag, repositoryImpl))
    })
  )
/*
  Layer.merge(
    Layer.effect(
      TagsRepositoryTag,
      Effect.gen(function* () {
        const restClient = yield* RestClientTag;
        const repositoryImpl = repositoryRest(restClient); // Single instance
        return repositoryImpl;
      })
    ),
    Layer.effect(
      EntriesRepositoryTag,
      Effect.gen(function* () {
        const restClient = yield* RestClientTag;
        const repositoryImpl = repositoryRest(restClient); // Single instance
        return repositoryImpl;
      })
    )
  )
*/
