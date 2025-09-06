import {describe, expect, it} from '@effect/vitest';
import {makeTag} from './Tag.ts';
import {id} from './identity/Identifier.ts';

describe('domain model Tag', () => {
  it('tag can be created', () => {
    const t = makeTag('example', id(123));
    expect(t).toEqual({label: 'example', ownerId: {value: 123}})
  })
})
