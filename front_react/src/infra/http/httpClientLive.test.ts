import {describe, expect, it} from '@effect/vitest';
import {Effect} from 'effect';
import {HttpClient} from './HttpClient.ts';
import httpClientLive from './httpClientLive.ts';

describe('HttpClient', () => {
  describe('Unit tests', () => {
    it.effect('Live instance can be instantiated', () => {
      const program = Effect.gen(function* () {
        const rest = yield* HttpClient;
        expect(rest).not.toBeNull()
      })
      return Effect.provide(program, httpClientLive);
    });
  });
  describe('Integration tests', () => {
    it.effect('get request should success', () => {
      const program = Effect.gen(function* () {
        const rest = yield* HttpClient;
        const body = yield* rest.execute('GET', 'https://echo.free.beeceptor.com/sample-request?author=beeceptor');
        expect(body).not.toBeNull()
        expect(body).toHaveProperty('parsedQueryParams');
        const obj = body as Record<string, unknown>;
        expect(obj['parsedQueryParams']).toEqual({'author': 'beeceptor'});
      })
      return Effect.provide(program, httpClientLive);
    });
    it.effect('post request should success', () => {
      const program = Effect.gen(function* () {
        const rest = yield* HttpClient;
        const body = yield* rest.execute('POST', 'https://echo.free.beeceptor.com/sample-request?author=beeceptor', JSON.stringify({'author': 'beeceptor'}));
        expect(body).not.toBeNull()
        expect(body).toHaveProperty('parsedQueryParams');
        const obj = body as Record<string, unknown>;
        expect(obj['parsedQueryParams']).toEqual({'author': 'beeceptor'});
      })
      return Effect.provide(program, httpClientLive);
    });
  });
});
