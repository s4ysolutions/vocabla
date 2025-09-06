import {describe, it, expect} from '@effect/vitest';
import {Effect} from 'effect';

describe('HelloWorld', () => {
  it.effect('returns hello', () =>
    Effect.succeed('hello').pipe(
      Effect.map((result) => {
        expect(result).toEqual('hello');
      })
    ));
});
