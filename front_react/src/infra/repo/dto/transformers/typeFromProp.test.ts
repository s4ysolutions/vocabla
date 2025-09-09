import {describe, expect, it} from '@effect/vitest';
import {Schema} from 'effect';
import {typeFromProp} from './typeFromProp.ts';

describe('TypeFromProp', () => {
  type DTO = string
  type AsProp = {
    prop: DTO
  }
  it('given valid prop success', () => {
    const prop1: AsProp = {
      prop: 'example'
    }
    const extracted: DTO = Schema.decodeSync(typeFromProp('prop', Schema.String))(prop1)
    expect(extracted).toEqual('example')
  });
  it('given invalid prop success', () => {
    const prop1 = {
      prop0: 'example'
    }
    expect(()=> Schema.decodeSync(typeFromProp('prop', Schema.String))(prop1)).toThrow()
  })
})
