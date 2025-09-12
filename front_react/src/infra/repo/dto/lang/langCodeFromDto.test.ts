import {describe, it, expect} from '@effect/vitest';
import {Schema} from 'effect';
import {langCodeFromDto} from './langCodeFromDto.ts';
import {LangCode} from '../../../../domain/LangCode.ts';

describe('langCodeFromDto', () => {
  it('dto -> domain', () => {
    // arrange
    const dto = 'en'
    // act
    const domain: LangCode = Schema.decodeSync(langCodeFromDto)(dto)
    // assert
    expect(domain).toEqual(LangCode('en'))
  })
  it('domain -> dto', () => {
    // arrange
    const domain = LangCode('en')
    // act
    const dto: string = Schema.encodeSync(langCodeFromDto)(domain)
    // assert
    expect(dto).toEqual('en')
  })
})
