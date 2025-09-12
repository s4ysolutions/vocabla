import {describe, it, expect} from '@effect/vitest';
import {
  decodeGetEntryResponse,
  encodeGetEntryResponse,
  type GetEntryResponse
} from './GetEntryResponse.ts';
import {Effect, Option} from 'effect';
import {Definition, Entry} from '../../../../domain/Entry.ts';
import {Localized} from '../../../../domain/Localized.ts';
import {Identifier} from '../../../../domain/identity/Identifier.ts';
import type {Student} from '../../../../domain/Student.ts';
import {LangCode} from '../../../../domain/LangCode.ts';

describe('GetEntryResponse', () => {
  it('dto -> domain', () => {
    const response: GetEntryResponse = {
      entry: {
        headword: {word: 'dog', langCode: 'en'}, definitions: [
          {definition: 'A domesticated carnivorous mammal', langCode: 'en'},
          {definition: 'Un mammifère carnivore domestiqué', langCode: 'fr'}
        ], ownerId: 42
      }
    };

    const domainOpt: Option.Option<Entry> = Effect.runSync(decodeGetEntryResponse(response))
    if (Option.isSome(domainOpt)) {
      console.log(domainOpt.value)
      console.log(domainOpt.value.definitions)
      expect(domainOpt.value).toEqual(
        Entry(Localized(LangCode('en'), 'dog'), [
            Definition(Localized(LangCode('en'), 'A domesticated carnivorous mammal')),
            {localized: Localized(LangCode('fr'), 'Un mammifère carnivore domestiqué')}
          ], Identifier<Student>(42)
        )
      )
    }
    const encoded = Effect.runSync(encodeGetEntryResponse(domainOpt))
    expect(encoded).toEqual(response)
  })
  it('domain -> dto', () => {
    const domainOpt: Option.Option<Entry> = Option.some(
      Entry(Localized(LangCode('en'), 'cat'), [
          Definition(Localized(LangCode('en'), 'A small domesticated carnivorous mammal')),
          Definition(Localized(LangCode('fr'), 'Un petit mammifère carnivore domestiqué'))
        ], Identifier<Student>(7)
      )
    );

    const response: GetEntryResponse = Effect.runSync(encodeGetEntryResponse(domainOpt))
    expect(response).toEqual({
      entry: {
        headword: {word: 'cat', langCode: 'en'}, definitions: [
          {definition: 'A small domesticated carnivorous mammal', langCode: 'en'},
          {definition: 'Un petit mammifère carnivore domestiqué', langCode: 'fr'}
        ], ownerId: 7
      }
    })

    const decodedOpt = Effect.runSync(decodeGetEntryResponse(response))
    expect(decodedOpt).toEqual(domainOpt)
  })
});
