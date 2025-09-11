import {describe, it, expect} from '@effect/vitest';
import {Effect} from 'effect';
import {map} from 'effect/Effect';

describe('dual map api', () => {
  it.effect('data last, partially', () => {
    type A = number
    type B = string

    const f = (a: A): B => a.toString()
    const mapLast = map<A, B>(f)
    const input: Effect.Effect<number> = Effect.succeed(1)
    const result: Effect.Effect<string>  = mapLast(input)

    return Effect.map(result, b => expect(b).toBe('1'))
  })

  it.effect('data last', () => {
    type A = number
    type B = string

    const f = (a: A): B => a.toString()
    const a: Effect.Effect<number> = Effect.succeed(1)
    const b: Effect.Effect<string>  = map(f)(a)

    return Effect.map(b, b => expect(b).toBe('1'))
  })

  it.effect('data first', () => {
    type A = number
    type B = string

    const f = (a: A): B => a.toString()
    const a: Effect.Effect<number> = Effect.succeed(1)
    const b: Effect.Effect<string>  = map(a, f)

    return Effect.map(b, b => expect(b).toBe('1'))
  })
})
