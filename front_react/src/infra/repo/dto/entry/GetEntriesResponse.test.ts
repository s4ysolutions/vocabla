import {describe, it, expect} from '@effect/vitest';
import {
  decodeGetEntriesResponse,
  type GetEntriesResponse
} from './GetEntriesResponse.ts';
import {Effect} from 'effect';
import {Definition, Entry} from '../../../../domain/Entry.ts';
import {Localized} from '../../../../domain/Localized.ts';
import {Identifier} from '../../../../domain/identity/Identifier.ts';
import {Identified} from '../../../../domain/identity/Identified.ts';
import type {Student} from '../../../../domain/Student.ts';
import {LangCode} from '../../../../domain/LangCode.ts';

describe('GetEntriesResponse', () => {
  it('dto -> domain', () => {
    const response: GetEntriesResponse = {
      entries: [
        {
          id: 42,
          e: {
            headword: {word: 'dog', langCode: 'en'},
            definitions: [
              {definition: 'A domesticated carnivorous mammal', langCode: 'en'},
              {definition: 'Un mammifère carnivore domestiqué', langCode: 'fr'}
            ],
            ownerId: 1
          }
        },
        {
          id: 43,
          e: {
            headword: {word: 'cat', langCode: 'en'},
            definitions: [
              {definition: 'A small domesticated carnivorous mammal', langCode: 'en'},
              {definition: 'Un petit mammifère carnivore domestiqué', langCode: 'fr'}
            ],
            ownerId: 1
          }
        }
      ]
    };

    const domain: {readonly entries: ReadonlyArray<Identified<Entry>>} = Effect.runSync(decodeGetEntriesResponse(response));

    expect(domain.entries).toHaveLength(2);

    // Test first entry
    expect(domain.entries[0]).toEqual(
      Identified(42, Entry(
        Localized(LangCode('en'), 'dog'),
        [
          Definition(Localized(LangCode('en'), 'A domesticated carnivorous mammal')),
          Definition(Localized(LangCode('fr'), 'Un mammifère carnivore domestiqué'))
        ],
        Identifier<Student>(1)
      ))
    );

    // Test second entry
    expect(domain.entries[1]).toEqual(
      Identified(43, Entry(
        Localized(LangCode('en'), 'cat'),
        [
          Definition(Localized(LangCode('en'), 'A small domesticated carnivorous mammal')),
          Definition(Localized(LangCode('fr'), 'Un petit mammifère carnivore domestiqué'))
        ],
        Identifier<Student>(1)
      ))
    );
  });

  it('should decode empty entries array', () => {
    const response: GetEntriesResponse = {
      entries: []
    };

    const domain: {readonly entries: ReadonlyArray<Identified<Entry>> } = Effect.runSync(decodeGetEntriesResponse(response));

    expect(domain.entries).toEqual([]);
  });

  it('should decode single entry', () => {
    const response: GetEntriesResponse = {
      entries: [
        {
          id: 99,
          e: {
            headword: {word: 'hello', langCode: 'en'},
            definitions: [
              {definition: 'A greeting', langCode: 'en'}
            ],
            ownerId: 2
          }
        }
      ]
    };

    const domain: {readonly entries: ReadonlyArray<Identified<Entry>> } = Effect.runSync(decodeGetEntriesResponse(response));

    expect(domain.entries).toHaveLength(1);
    expect(domain.entries[0]).toEqual(
      Identified(99, Entry(
        Localized(LangCode('en'), 'hello'),
        [
          Definition(Localized(LangCode('en'), 'A greeting'))
        ],
        Identifier<Student>(2)
      ))
    );
  });

  it('should handle entries with multiple definitions in different languages', () => {
    const response: GetEntriesResponse = {
      entries: [
        {
          id: 100,
          e: {
            headword: {word: 'water', langCode: 'en'},
            definitions: [
              {definition: 'A transparent liquid', langCode: 'en'},
              {definition: 'Un liquide transparent', langCode: 'fr'},
              {definition: 'Una sustancia líquida transparente', langCode: 'es'}
            ],
            ownerId: 3
          }
        }
      ]
    };

    const domain: { entries: ReadonlyArray<Identified<Entry>> } = Effect.runSync(decodeGetEntriesResponse(response));
    expect(domain.entries).toHaveLength(1);

    expect(domain.entries[0]!.e.definitions).toHaveLength(3);
    expect(domain.entries[0]!.e.definitions[0]).toEqual(
      Definition(Localized(LangCode('en'), 'A transparent liquid'))
    );
    expect(domain.entries[0]!.e.definitions[1]).toEqual(
      Definition(Localized(LangCode('fr'), 'Un liquide transparent'))
    );
    expect(domain.entries[0]!.e.definitions[2]).toEqual(
      Definition(Localized(LangCode('es'), 'Una sustancia líquida transparente'))
    );
  });

  it('should handle entries with different owners', () => {
    const response: GetEntriesResponse = {
      entries: [
        {
          id: 200,
          e: {
            headword: {word: 'book', langCode: 'en'},
            definitions: [
              {definition: 'A written work', langCode: 'en'}
            ],
            ownerId: 10
          }
        },
        {
          id: 201,
          e: {
            headword: {word: 'pen', langCode: 'en'},
            definitions: [
              {definition: 'A writing instrument', langCode: 'en'}
            ],
            ownerId: 20
          }
        }
      ]
    };

    const domain: {readonly entries: ReadonlyArray<Identified<Entry>> } = Effect.runSync(decodeGetEntriesResponse(response));

    expect(domain.entries).toHaveLength(2);
    expect(domain.entries[0]!.e.ownerId).toEqual(Identifier<Student>(10));
    expect(domain.entries[1]!.e.ownerId).toEqual(Identifier<Student>(20));
  });

  it('should fail on invalid entry structure', () => {
    const invalidResponse = {
      entries: [
        {
          id: 'not-a-number', // Invalid ID type
          e: {
            headword: {word: 'test', langCode: 'en'},
            definitions: [],
            ownerId: 1
          }
        }
      ]
    } as unknown as GetEntriesResponse;

    expect(() => Effect.runSync(decodeGetEntriesResponse(invalidResponse))).toThrow();
  });

  it('should fail on missing required fields', () => {
    const invalidResponse = {
      entries: [
        {
          id: 42,
          e: {
            // Missing headword
            definitions: [
              {definition: 'A definition', langCode: 'en'}
            ],
            ownerId: 1
          }
        }
      ]
    } as unknown as GetEntriesResponse;

    expect(() => Effect.runSync(decodeGetEntriesResponse(invalidResponse))).toThrow();
  });
});
