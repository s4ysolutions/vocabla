import {describe, it, expect} from '@effect/vitest';
import {Effect, Option, Schema} from 'effect';
import {nullOrFromProp} from './nullOrFromProp.ts';

describe('NullOrFromProp', () => {
  const decoder =
    Schema.decode(nullOrFromProp<number, string>('prop', Schema.NumberFromString))
  it('prop exists', () => {
    const dto = {prop: '123'}
    const effect = decoder(dto)
    const decoded = Effect.runSync(effect)
    expect(Option.isSome(decoded)).toBeTruthy()
    expect(decoded).toEqual({value: 123})
  })
  it('prop does not exist', () => {
    const dto = {otherProp: 'value'}
    const effect = decoder(dto as unknown as { [x: string]: string; })
    expect(() => Effect.runSync(effect)).toThrow()
  })
  it('prop is undefined', () => {
    const dto = {prop: undefined}
    const effect = decoder(dto as unknown as { [x: string]: string; })
    expect(() => Effect.runSync(effect)).toThrow()
  })
  it('prop is null', () => {
    const dto = {prop: null}
    const effect = decoder(dto as unknown as { [x: string]: string; })
    const decoded = Effect.runSync(effect)
    expect(Option.isNone(decoded)).toBeTruthy()
  })
})
