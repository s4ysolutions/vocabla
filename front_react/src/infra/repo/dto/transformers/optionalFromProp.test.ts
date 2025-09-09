import {describe, it, expect} from '@effect/vitest';
import {Effect, Option, Schema} from 'effect';
import {optionalFromProp} from './optionalFromProp.ts';

describe('OptionalFromProp', () => {
  const decoder =
    Schema.decode(optionalFromProp<number, string>('prop', Schema.NumberFromString))
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
    const decoded = Effect.runSync(effect)
    expect(Option.isNone(decoded)).toBeTruthy()
  })
  it ('prop is undefined', () => {
    const dto = {prop: undefined}
    const effect = decoder(dto as unknown as { [x: string]: string; })
    const decoded = Effect.runSync(effect)
    expect(Option.isNone(decoded)).toBeTruthy()
  })
  it ('prop is null', () => {
    const dto = {prop: null}
    const effect = decoder(dto as unknown as { [x: string]: string; })
    const decoded = Effect.runSync(effect)
    expect(Option.isNone(decoded)).toBeTruthy()
  })
})
