import {describe, expect, it} from '@effect/vitest';
import {Effect, Layer, Schema} from 'effect';
import {REST} from './REST.ts';
import {restLive} from './restLive.ts';
import httpClientLive from '../http/httpClientLive.ts';

describe('REST', () => {
  const restLiveProvider = restLive.pipe(
    Layer.provide(httpClientLive)
  )//Layer.provide(restLive, httpClientLive)
  describe('Unit tests', () => {
    it.effect('Live instance can be instantiated', () =>
      Effect.gen(function* () {
        const rest = yield* REST;
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
        const rest = yield* REST;
        const response = yield* rest.post('https://echo.free.beeceptor.com/sample-request?author=beeceptor', {'author': 'beeceptor'}, querySchema, responseSchema);
        expect(response).not.toBeNull()
        expect(response).toEqual({parsedQueryParams: {author: 'beeceptor'}});
      }).pipe(
        Effect.provide(restLiveProvider),
      ))
  });
});
