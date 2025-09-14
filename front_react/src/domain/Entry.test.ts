import {describe, expect, it} from '@effect/vitest';
import {Definition, Entry} from './Entry.ts';
import {Identifier} from './identity/Identifier.ts';
import type {Student} from './Student.ts';
import {Localized} from './Localized.ts';
import {LangCode} from './LangCode.ts';

describe('Entry', () => {
  describe('Entry', () => {
    it('type should exist', () => {
      const e: Entry = Entry(
        Localized(LangCode('en'), 'word'),
        [Definition(Localized(LangCode('en'), 'word'))],
        Identifier<Student>(1234))
      expect(e).toEqual(
        {
          word: {langCode: 'en', s: 'word'},
          definitions: [{localized: {langCode: 'en', s: 'word'}, source: undefined}],
          ownerId: 1234
        }
      )
    })
  })
  describe('Definition', () => {
    it('type should exist', () => {
      const d: Definition = Definition(Localized(LangCode('en'), 'word'));
      expect(d).toEqual(
        {localized: {langCode: 'en', s: 'word'}, source: undefined}
      )
    })
  })
});

