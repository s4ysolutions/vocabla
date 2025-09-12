import {describe, expect, it} from '@effect/vitest';
import type {DefinitionDTO} from './definitionDto.ts';
import {Schema} from 'effect';
import {definitionFromDto} from './definitionFromDto.ts';
import type {EntryDTO} from './entryDto.ts';
import {entryFromDto} from './entryFromDto.ts';
import {Definition, type Entry} from '../../../../domain/Entry.ts';
import {Identifier} from '../../../../domain/identity/Identifier.ts';
import type {Student} from '../../../../domain/Student.ts';
import {Localized} from '../../../../domain/Localized.ts';
import {LangCode} from '../../../../domain/LangCode.ts';

describe('entryFromDto', () => {
  it('definitionFromDto', () => {
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
  })
  it('entryFromDto encode', () => {
    const dto: EntryDTO = {
      headword: {langCode: 'en', word: 'dog'},
      definitions: [
        {langCode: 'en', definition: 'A domesticated carnivorous mammal'},
        {langCode: 'fr', definition: 'Un mammifère carnivore domestiqué'}
      ],
      ownerId: 42
    }
    // act
    const domain: Entry = Schema.decodeSync(entryFromDto)(dto);
    // assert
    expect(domain).toEqual({
      word: Localized(LangCode('en'), 'dog'),
      definitions: [
        Definition(Localized(LangCode('en'), 'A domesticated carnivorous mammal')),
        Definition(Localized(LangCode('fr'), 'Un mammifère carnivore domestiqué'))
      ],
      ownerId: Identifier<Student>(42)
    })
    expect(Schema.encodeSync(entryFromDto)(domain)).toEqual(dto)
  })
  it('entryFromDto decode', () => {
    const domain: Entry = {
      word: Localized(LangCode('en'), 'dog'),
      definitions: [
        Definition(Localized(LangCode('en'), 'A domesticated carnivorous mammal')),
        Definition(Localized(LangCode('fr'), 'Un mammifère carnivore domestiqué'))
      ],
      ownerId: Identifier<Student>(42)
    }
    // act
    const dto: EntryDTO = Schema.encodeSync(entryFromDto)(domain);
    // assert
    expect(dto).toEqual({
      headword: {langCode: 'en', word: 'dog'},
      definitions: [
        {langCode: 'en', definition: 'A domesticated carnivorous mammal'},
        {langCode: 'fr', definition: 'Un mammifère carnivore domestiqué'}
      ],
      ownerId: 42
    })
    expect(Schema.decodeSync(entryFromDto)(dto)).toEqual(domain)
  })
})
