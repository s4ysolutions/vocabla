import {describe, it, expect} from '@effect/vitest';
import {
  decodeGetEntryResponse,
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
    expect(Option.isSome(domainOpt)).toBe(true)
    if (Option.isSome(domainOpt)) {
      expect(domainOpt.value).toEqual(
        Entry(Localized(LangCode('en'), 'dog'), [
            Definition(Localized(LangCode('en'), 'A domesticated carnivorous mammal')),
            {localized: Localized(LangCode('fr'), 'Un mammifère carnivore domestiqué')}
          ], Identifier<Student>(42)
        )
      )
    }
  })
});
