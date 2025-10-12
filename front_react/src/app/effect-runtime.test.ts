import {describe, expect, it} from '@effect/vitest';

import {Effect} from 'effect';
import {promiseAppEffect} from './effect-runtime.ts';

describe('Effect runtime', () => {

  it('effect should return value as promise', async () => {
    const result = await promiseAppEffect(Effect.succeed(42));

    expect(result).toBe(42);
  })

  // type A = number
  // type B = []
  // Functor<Effect<number>> = Hom(Effect<number>, Effect<[]>)
  //   fmap<A, B>(Effect<number>, number => []): Effect[[]]

  it('effect map should return value as promise', async () => {
    const result = await promiseAppEffect(
      Effect.succeed(42).pipe(
        Effect.map(x => [x, x + 1])
      ));
    expect(result).toEqual([42, 43]);
  })

  // Monad<Effect<number> = Hom(Effect<number>, Effect<[]>)
  //   flatMap<A, B>(Effect<number>, number => Effect<[]>): Effect[[]]
  it('effect flatMap should return value as promise', async () => {
    let side = false
    const result = await promiseAppEffect(
      Effect.succeed(42).pipe(
        Effect.flatMap(x => Effect.sync(() => {
          side = true
          return [x, x + 1]
        }))
      ));
    expect(result).toEqual([42, 43]);
    expect(side).toBe(true)
  })

  // Functor Effect<number> = Hom(Effect<number>, Effect<Effect<[]>>)
  //  fmap<A, B>(Effect<number>, number => Effect<[]>): Effect<Effect[[]]>
  it('effect map effect should not run', async () => {
    let side = false
    const result = await promiseAppEffect(
      Effect.succeed(42).pipe(
        Effect.map(x => Effect.sync(() => {
          side = true
          return [x, x + 1]
        }))
      ));
    expect(result).not.toEqual(44);
    expect(side).toBe(false)
  })
})
