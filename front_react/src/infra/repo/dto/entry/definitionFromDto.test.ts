import {describe, expect, it} from '@effect/vitest';
import type {DefinitionDTO} from './definitionDto.ts';
import {Schema} from 'effect';
import {definitionFromDto} from './definitionFromDto.ts';

describe('definitionFromDto', () => {
  it('dto -> domain', () => {
    const dto: DefinitionDTO = {
      definition: 'A domesticated carnivorous mammal',
      langCode: 'en'
    }
    // act
    const domain = Schema.decodeSync(definitionFromDto)(dto)
    // assert
    expect(domain).toEqual({
      localized: {
        s: 'A domesticated carnivorous mammal',
        langCode: 'en'
      }
    })
    expect(Schema.encodeSync(definitionFromDto)(domain)).toEqual(dto)
  })
})
