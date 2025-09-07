import {describe, expect, it} from '@effect/vitest';
import {Layer, Option} from 'effect';
import httpClientLayer from '../http/httpClientLive.ts';
import {Effect} from 'effect';
import {TagsRepositoryTag} from '../../app-repo/TagsRepository.ts';
import {id} from '../../domain/identity/Identifier.ts';
import {makeTag} from '../../domain/Tag.ts';
import {
  type CreateTagResponse, decodeCreateTagResponse, decodeGetTagResponse,
  type GetTagResponse,
  repositoryRestLayer,
} from './repositoryRest.ts';
import {restClientLayer} from '../rest/restClientLive.ts';

describe('repositoryRest', () => {
  const layer = repositoryRestLayer.pipe(
    Layer.provide(restClientLayer),
    Layer.provide(httpClientLayer)
  )
  describe('schemas', () => {
    it('schemaCreateTagResponse', () => {
      const response: CreateTagResponse = {tagId: 123};
      const tageId = Effect.runSync(decodeCreateTagResponse(response));
      expect(tageId).toEqual({value: 123});
    });
    it('schemaCreateTagResponse error', () => {
      const response = {tagId: 'abc'} as unknown as { [x: string]: number; };
      const decode = decodeCreateTagResponse(response);
      expect(() => Effect.runSync(decode)).toThrowError();
    });
    it('schemaGetTagResponse', () => {
      const response: GetTagResponse = {tag: {label: 'test', ownerId: 1}};
      const tag = Effect.runSync(decodeGetTagResponse(response));
      expect(Option.isSome(tag)).toBeTruthy();
      if (Option.isSome(tag)) {
        expect(tag.value).toEqual(makeTag('test', id(1)));
      }
      if (Option.isNone(tag)) {
        throw new Error('Expected Some, got None');
      }
    });
    it('schemaGetTagResponse null', () => {
      const response: GetTagResponse = {tagx: {label: 'test', ownerId: 1}} as unknown as GetTagResponse;
      expect(() => Effect.runSync(decodeGetTagResponse(response))).toThrowError();
    })
  });
  describe('integration tests', () => {
    it.effect('createTag', () => {
      const program = Effect.gen(function* () {
        const tagsRepository = yield* TagsRepositoryTag;
        const tagId = yield* tagsRepository.createTag(makeTag('test', id(1)));
        console.log('Created tag with id:', tagId);
      });
      return Effect.provide(program, layer);
    })
    it.effect('getTag', () => {
      const program = Effect.gen(function* () {
        const tagsRepository = yield* TagsRepositoryTag;
        const label = 'test-' + Math.floor(Math.random() * 10000);
        const tagId = yield* tagsRepository.createTag(makeTag(label, id(1)));
        const tag = yield* tagsRepository.getTag(tagId);
        expect(tag._tag).toBe('Some');
        if (tag._tag === 'Some') {
          expect(tag.value.label).toBe(label);
          expect(tag.value.ownerId).toEqual(id(1));
        }
      });
      return Effect.provide(program, layer);
    })
  })
})
