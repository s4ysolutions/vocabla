import {describe, expect, it} from '@effect/vitest';
import {Effect} from 'effect';
import {HttpClientTag} from './HttpClient.ts';
import HttpClientLive from './HttpClientLive.ts';

describe('HttpClient', () => {
  describe('Unit tests', () => {
    it.effect('Live instance can be instantiated', () => {
      const program = Effect.gen(function* () {
        const client = yield* HttpClientTag;
        expect(client).not.toBeNull()
      })
      return Effect.provide(program, HttpClientLive.layer);
    });
  });
  describe('Integration tests', () => {
    it.effect('get request should success', () => {
      const program = Effect.gen(function* () {
        const client = yield* HttpClientTag;
        const body = yield* client.execute('GET', 'https://echo.free.beeceptor.com/sample-request?author=beeceptor');
        expect(body).not.toBeNull()
        expect(body).toHaveProperty('parsedQueryParams');
        const obj = body as Record<string, unknown>;
        expect(obj['parsedQueryParams']).toEqual({'author': 'beeceptor'});
      })
      return Effect.provide(program, HttpClientLive.layer);
    });
    it.effect('post request should success', () => {
      const program = Effect.gen(function* () {
        const client = yield* HttpClientTag;
        const body = yield* client.execute('POST', 'https://echo.free.beeceptor.com/sample-request?author=beeceptor', JSON.stringify({'author': 'beeceptor'}));
        expect(body).not.toBeNull()
        expect(body).toHaveProperty('parsedQueryParams');
        const obj = body as Record<string, unknown>;
        expect(obj['parsedQueryParams']).toEqual({'author': 'beeceptor'});
      })
      return Effect.provide(program, HttpClientLive.layer);
    });
  });
});
