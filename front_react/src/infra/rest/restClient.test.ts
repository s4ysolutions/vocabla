import {describe, expect, it} from '@effect/vitest';
import {Effect, Layer, Schema} from 'effect';
import {RestClientTag} from './RestClient.ts';
import RestClientLive from './RestClientLive.ts';
import HttpClientLive from '../http/HttpClientLive.ts';

describe('REST', () => {
  const restLiveProvider = Layer.provide( RestClientLive.layer, HttpClientLive.layer)

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
