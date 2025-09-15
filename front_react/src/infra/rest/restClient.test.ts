import {describe, expect, it} from '@effect/vitest';
import {Effect, Layer, Schema} from 'effect';
import {RestClientTag} from './RestClient.ts';
import httpClientLive from '../http/httpClientLive.ts';
import {restClientLayer} from './restClientLive.ts';

describe('REST', () => {
  const restLiveProvider = restClientLayer.pipe(Layer.provide(httpClientLive))

  describe('Unit tests', () => {
    it.effect('Live instance can be instantiated', () =>
      Effect.gen(function* () {
        const rest = yield* RestClientTag;
        expect(rest).not.toBeNull()
        expect(rest).toHaveProperty('post')
      }).pipe(
        Effect.provide(restLiveProvider),
      ))
  });

  describe('Integration tests', () => {
    const querySchema = Schema.Struct({
      author: Schema.String
    })
    const responseSchema = Schema.Struct({
      parsedQueryParams: querySchema
    })
    it.effect('post request should success', () =>
      Effect.gen(function* () {
        const rest = yield* RestClientTag;
        const response = yield* rest.post({
          url: 'https://echo.free.beeceptor.com/sample-request?author=beeceptor',
          body: {'author': 'beeceptor'},
          decoder: Schema.decodeUnknown(responseSchema)
        });
        expect(response).not.toBeNull()
        expect(response).toEqual({parsedQueryParams: {author: 'beeceptor'}});
      }).pipe(
        Effect.provide(restLiveProvider),
      ))
  });
});
