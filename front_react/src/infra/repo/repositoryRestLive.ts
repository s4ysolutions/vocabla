import {Context, Effect, Layer} from 'effect';
import {RestClientTag} from '../rest/RestClient.ts';
import {EntriesRepositoryTag} from '../../app-repo/EntriesRepository.ts';
import {LangRepositoryTag} from '../../app-repo/LangRepository.ts';
import {makeRepositoryRest} from './repositoryRest.ts';
import {restClientLive} from '../rest/restClientLive.ts';
import {LearningSettingsRepositoryTag} from '../../app-repo/LearningSettingsRepository.ts';

export const repositoryRestLayer: Layer.Layer<EntriesRepositoryTag | LangRepositoryTag | LearningSettingsRepositoryTag, never, RestClientTag> =
  Layer.effectContext(
    Effect.gen(function* () {
      const restClient = yield* RestClientTag
      const repositoryImpl = makeRepositoryRest(restClient) // Single instance

      return Context.empty()
        .pipe(Context.add(EntriesRepositoryTag, repositoryImpl))
        .pipe(Context.add(LangRepositoryTag, repositoryImpl))
        .pipe(Context.add(LearningSettingsRepositoryTag, repositoryImpl))
    })
  )

export const repositoryRestLive: Layer.Layer<EntriesRepositoryTag | LangRepositoryTag | LearningSettingsRepositoryTag> = repositoryRestLayer.pipe(
  Layer.provide(restClientLive)
)
