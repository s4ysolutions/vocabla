import {describe, expect, it} from '@effect/vitest';
import {Schema} from 'effect';
import {schemaTag} from './Tag.ts';

describe('domain model Tag', () => {
  it('tag can be created from valid object', () => {
    const t = Schema.decodeSync(schemaTag)({label: 'example', ownerId: 123})
    expect(t).toEqual({label: 'example', ownerId: 123})
  })
  it('tag can not be created from inval valid ownerId', () => {
    const dto = {label: 'example', ownerId: '123'}
    expect(() => Schema.decodeUnknownSync(schemaTag)(dto)).toThrowError();
  })
})
