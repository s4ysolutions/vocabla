import {describe, it} from '@effect/vitest';
import {Layer} from 'effect';
import httpClientLayer from '../http/httpClientLive.ts';
import {Effect} from 'effect';
import {TagsRepositoryTag} from '../../app-repo/TagsRepository.ts';
import {id} from '../../domain/identity/Identifier.ts';
import {makeTag} from '../../domain/Tag.ts';
import {repositoryRestLayer} from './repositoryRest.ts';
import {restClientLayer} from '../rest/restClientLive.ts';

describe('repositoryRest', () => {
  const layer = repositoryRestLayer.pipe(
    Layer.provide(restClientLayer),
    Layer.provide(httpClientLayer)
  )

  describe('TagsRepository', () => {
    it.effect('createTag', () => {
      const program = Effect.gen(function* () {
        const tagsRepository = yield* TagsRepositoryTag;
        const tagId = yield* tagsRepository.createTag(makeTag('test', id(1)));
        console.log('Created tag with id:', tagId);
    });
    return Effect.provide(program, layer);
  })
})
})
