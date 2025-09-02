import {describe, expect, it} from '@effect/vitest';
import {definition, type Definition, entry, type Entry} from './Entry.ts';
import {localized} from './Localized.ts';
import {id} from './identity/Identifier.ts';

describe('Entry', () => {
  describe('Entry', () => {
    it('type should exist', () => {
      const e: Entry = entry(localized('en', 'word'), [definition(localized('en', 'word'))], id(1234))
      expect(e).toEqual(
        {
          word: {langCode: 'en', s: 'word'},
          definitions: [{localized: {langCode: 'en', s: 'word'}, source: undefined}],
          ownerId: {value: 1234}
        }
      )
    })
  })
  describe('Definition', () => {
    it('type should exist', () => {
      const d: Definition = definition(localized('en', 'word'));
      expect(d).toEqual(
        {localized: {langCode: 'en', s: 'word'}, source: undefined}
      )
    })
  })
});
