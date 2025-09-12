import {describe, it, expect} from '@effect/vitest';
import {Option, Effect} from 'effect';
import {decodeGetTagResponse, type GetTagResponse} from './GetTagResponse.ts';
import type {Tag} from '../../../../domain/Tag.ts';

describe('GetTagResponse', () => {
  it('tag present', () => {
    // arrange
    const response: GetTagResponse = {tag: {label: 'tag1', ownerId: 42}}
    // act
    const domainOpt: Option.Option<Tag> = Effect.runSync(decodeGetTagResponse(response))

    // assert
    expect(Option.isSome(domainOpt)).toBeTruthy()
    if (Option.isSome(domainOpt)) {
      expect(domainOpt.value).toEqual({label: 'tag1', ownerId: 42})
    }
  })
  it('tag absent', () => {
    // arrange
    const response: GetTagResponse = {tag: null}
    // act
    const domainOpt = Effect.runSync(decodeGetTagResponse(response))
    // assert
    expect(Option.isNone(domainOpt)).toBeTruthy()
  })
  it('invalid tag', () => {
    // arrange
    const response = {tag: {label: 'tag1', ownerId: 'not-a-number'}}
    // act
    const effect = decodeGetTagResponse(response as unknown as GetTagResponse)
    // assert
    expect(() => Effect.runSync(effect)).toThrow()
  })
})
