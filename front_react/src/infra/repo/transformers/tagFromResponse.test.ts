import {describe, it, expect} from '@effect/vitest';
import type {components} from '../../rest/types.ts';
import {Schema} from 'effect';
import {tagFromResponse} from './tagFromResponse.ts';

type TagDTO = components['schemas']['Tag']

describe('TagFromResponse', () => {
  it('decode success', () => {
    const dto : TagDTO = {
      label: 'tag1',
      ownerId: 42
    }
    // act
    const domain = Schema.decodeSync(tagFromResponse)(dto)
    // assert
    expect(domain).toEqual({
      label: 'tag1',
      ownerId: {value: 42}
    })
  })
  it('decode fail', () => {
    const dto = {
      label: 'tag1',
      ownerId: 'not-a-number'
    }
    // act & assert
    expect(() => Schema.decodeUnknownSync(tagFromResponse)(dto)).toThrow()
  })
})
