import {Context, Effect, Layer} from 'effect';
import {RestClientTag} from '../rest/RestClient.ts';
import {TagsRepositoryTag} from '../../app-repo/TagsRepository.ts';
import {EntriesRepositoryTag} from '../../app-repo/EntriesRepository.ts';
import {LangRepositoryTag} from '../../app-repo/LangRepository.ts';
import {repositoryRest} from './repositoryRest.ts';
import {restClientLive} from '../rest/restClientLive.ts';

export const repositoryRestLayer: Layer.Layer<TagsRepositoryTag | EntriesRepositoryTag | LangRepositoryTag, never, RestClientTag> =
  Layer.effectContext(
    Effect.gen(function* () {
      const restClient = yield* RestClientTag
      const repositoryImpl = repositoryRest(restClient) // Single instance

      return Context.empty()
        .pipe(Context.add(TagsRepositoryTag, repositoryImpl))
        .pipe(Context.add(EntriesRepositoryTag, repositoryImpl))
        .pipe(Context.add(LangRepositoryTag, repositoryImpl))
    })
  )

export const repositoryRestLive: Layer.Layer<TagsRepositoryTag | EntriesRepositoryTag | LangRepositoryTag> = repositoryRestLayer.pipe(
  Layer.provide(restClientLive)
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
