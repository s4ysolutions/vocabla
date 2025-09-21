import {describe, expect, it} from '@effect/vitest';
import {Layer, Option} from 'effect';
import httpClientLive from '../http/httpClientLive.ts';
import {Effect} from 'effect';
import {TagsRepositoryTag} from '../../app-repo/TagsRepository.ts';
import {restClientLayer} from '../rest/restClientLive.ts';
import {repositoryRestLayer} from './repositoryRestLive.ts';
import {decodeGetTagResponse, type GetTagResponse} from './dto/tag/GetTagResponse.ts';
import {type CreateTagResponse, decodeCreateTagResponse} from './dto/tag/CreateTagResponse.ts';
import {decodeGetEntriesResponse, type GetEntriesResponse} from './dto/entry/GetEntriesResponse.ts';
import {Identifier} from '../../domain/identity/Identifier.ts';
import {Tag} from '../../domain/Tag.ts';
import {Definition, Entry} from '../../domain/Entry.ts';
import {Localized} from '../../domain/Localized.ts';
import {LangCode} from '../../domain/LangCode.ts';
import {EntriesFilter} from '../../domain/EntriesFilter.ts';
import type {Student} from '../../domain/Student.ts';
import {EntriesRepositoryTag} from '../../app-repo/EntriesRepository.ts';
import {Identified} from '../../domain/identity/Identified.ts';

describe('repositoryRest', () => {
  const layer: Layer.Layer<TagsRepositoryTag | EntriesRepositoryTag> = repositoryRestLayer.pipe(
    Layer.provide(restClientLayer),
    Layer.provide(httpClientLive)
  )
  describe('schemas', () => {
    describe('tags', () => {
      it('schemaCreateTagResponse', () => {
        const response: CreateTagResponse = {tagId: 123};
        const tageId = Effect.runSync(decodeCreateTagResponse(response));
        expect(tageId).toEqual(Identifier(123));
      });
      it('schemaCreateTagResponse error', () => {
        const response: CreateTagResponse = {tagId: 'abc'} as unknown as CreateTagResponse;
        const decode = decodeCreateTagResponse(response);
        expect(() => Effect.runSync(decode)).toThrowError();
      });
      it('schemaGetTagResponse data', () => {
        const response: GetTagResponse = {tag: {label: 'test', ownerId: 1}};
        const tag = Effect.runSync(decodeGetTagResponse(response));
        expect(Option.isSome(tag)).toBeTruthy();
        if (Option.isSome(tag)) {
          expect(tag.value).toEqual(Tag('test', Identifier(1)));
        }
        if (Option.isNone(tag)) {
          throw new Error('Expected Some, got None');
        }
      });
      it('schemaGetTagResponse null', () => {
        const response: GetTagResponse = {tag: null}
        const tag = Effect.runSync(decodeGetTagResponse(response));
        expect(Option.isNone(tag)).toBeTruthy();
      })
      it('schemaGetTagResponse absent', () => {
        const response: GetTagResponse = {tagx: {label: 'test', ownerId: 1}} as unknown as GetTagResponse;
        const tag = Effect.runSync(decodeGetTagResponse(response));
        expect(Option.isNone(tag)).toBeTruthy();
      })
    })
    describe('entries', () => {
      it('schemaGetEntriesResponse', () => {
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
                  {definition: 'A small domesticated carnivorous mammal', langCode: 'en'}
                ],
                ownerId: 1
              }
            }
          ]
        };

        const {entries} = Effect.runSync(decodeGetEntriesResponse(response))
        expect(entries).toHaveLength(2)

        // Verify first entry
        expect(entries[0]).toEqual(
          Identified<Entry>(42, Entry(
            Localized(LangCode('en'), 'dog'),
            [
              Definition(Localized(LangCode('en'), 'A domesticated carnivorous mammal')),
              Definition(Localized(LangCode('fr'), 'Un mammifère carnivore domestiqué'))
            ],
            Identifier<Student>(1)
          )))

        // Verify second entry
        expect(entries[1]).toEqual(
          Identified<Entry>(43, Entry(
              Localized(LangCode('en'), 'cat'),
              [
                Definition(Localized(LangCode('en'), 'A small domesticated carnivorous mammal'))
              ],
              Identifier<Student>(1)
            )
          ))
      });

      it('schemaGetEntriesResponse empty', () => {
        const response: GetEntriesResponse = {
          entries: []
        };

        const {entries} = Effect.runSync(decodeGetEntriesResponse(response));
        expect(entries).toEqual([]);
      });

      it('schemaGetEntriesResponse error - invalid id', () => {
        const response = {
          entries: [
            {
              id: 'not-a-number',
              e: {
                headword: {word: 'test', langCode: 'en'},
                definitions: [],
                ownerId: 1
              }
            }
          ]
        } as unknown as GetEntriesResponse;

        expect(() => Effect.runSync(decodeGetEntriesResponse(response))).toThrowError();
      });

      it('schemaGetEntriesResponse error - missing entry data', () => {
        const response = {
          entries: [
            {
              id: 42
              // Missing 'e' property
            }
          ]
        } as unknown as GetEntriesResponse;

        expect(() => Effect.runSync(decodeGetEntriesResponse(response))).toThrowError();
      });
    })
  });
  describe('integration tests', () => {
    describe('tags', () => {
      it.effect('createTag', () => {
        const program = Effect.gen(function* () {
          const tagsRepository = yield* TagsRepositoryTag;
          const tagId = yield* tagsRepository.createTag(Tag('test', Identifier(1)));
          console.log('Created tag with id:', tagId);
          expect(tagId).toBeDefined();
        });
        return Effect.provide(program, layer);
      })
      it.effect('getTag', () => {
        const program = Effect.gen(function* () {
          const tagsRepository = yield* TagsRepositoryTag;
          const label = 'test-' + Math.floor(Math.random() * 10000);
          const tagId = yield* tagsRepository.createTag(Tag(label, Identifier(1)));
          const tag = yield* tagsRepository.getTag(tagId);
          expect(tag._tag).toBe('Some');
          if (tag._tag === 'Some') {
            expect(tag.value.label).toBe(label);
            expect(tag.value.ownerId).toEqual(Identifier(1));
          }
        });
        return Effect.provide(program, layer);
      })
    })
    describe('entries', () => {
      it.effect('createEntry', () => {
        const program = Effect.gen(function* () {
          const entryRepository = yield* EntriesRepositoryTag;
          const entryId = yield* entryRepository.createEntry(Entry(Localized(LangCode('en'), 'dog'), [
            Definition(Localized(LangCode('en'), 'A domesticated carnivorous mammal')),
            Definition(Localized(LangCode('fr'), 'Un mammifère carnivore domestiqué'))
          ], Identifier<Student>(1)), []);
          console.log('Created entry with id:', entryId);
          expect(entryId).toBeDefined();
        });
        return Effect.provide(program, layer);
      })
      it.effect('getEntry', () => {
        const program = Effect.gen(function* () {
          const entryRepository = yield* EntriesRepositoryTag;
          const headword = 'cat-' + Math.floor(Math.random() * 10000);
          const entryId = yield* entryRepository.createEntry(Entry(Localized(LangCode('en'), headword), [
            Definition(Localized(LangCode('en'), 'A small domesticated carnivorous mammal')),
            Definition(Localized(LangCode('fr'), 'Un petit mammifère carnivore domestiqué'))
          ], Identifier<Student>(1)), []);
          const entryOpt = yield* entryRepository.getEntry(entryId);
          expect(entryOpt._tag).toBe('Some');
          if (entryOpt._tag === 'Some') {
            expect(entryOpt.value.word.s).toBe(headword);
            expect(entryOpt.value.word.langCode).toEqual(LangCode('en'));
            expect(entryOpt.value.definitions.length).toBe(2);
            expect(entryOpt.value.ownerId).toEqual(Identifier(1));
          }
        });
        return Effect.provide(program, layer);
      })
      it.effect('getEntries', () => {
        const program = Effect.gen(function* () {
          const entryRepository = yield* EntriesRepositoryTag;

          // Create some test entries
          const headword1 = 'dog-' + Math.floor(Math.random() * 10000);
          const headword2 = 'cat-' + Math.floor(Math.random() * 10000);

          const entryId1 = yield* entryRepository.createEntry(Entry(
            Localized(LangCode('en'), headword1),
            [
              Definition(Localized(LangCode('en'), 'A domesticated carnivorous mammal')),
              Definition(Localized(LangCode('fr'), 'Un mammifère carnivore domestiqué'))
            ],
            Identifier<Student>(1)
          ), []);

          const entryId2 = yield* entryRepository.createEntry(Entry(
            Localized(LangCode('en'), headword2),
            [
              Definition(Localized(LangCode('en'), 'A small domesticated carnivorous mammal'))
            ],
            Identifier<Student>(1)
          ), []);

          // Create filter for retrieving entries
          const filter = EntriesFilter([], [LangCode('en')], Option.none());

          // Get entries for owner
          const {entries} = yield* entryRepository.getEntriesByOwner(Identifier<Student>(1), filter);

          // Verify we got entries back (should include our created entries)
          expect(entries.length).toBeGreaterThanOrEqual(2);

          // Find our created entries in the results
          const foundEntry1 = entries.find(entry =>
            entry.id === entryId1 &&
            entry.e.word.s === headword1 &&
            entry.e.word.langCode === LangCode('en')
          )
          const foundEntry2 = entries.find(entry =>
            entry.id === entryId2 &&
            entry.e.word.s === headword2 &&
            entry.e.word.langCode === LangCode('en')
          )

          expect(foundEntry1).toBeDefined();
          expect(foundEntry2).toBeDefined();

          if (foundEntry1) {
            expect(foundEntry1.e.definitions.length).toBe(2);
            expect(foundEntry1.e.ownerId).toEqual(Identifier<Student>(1));
          }

          if (foundEntry2) {
            expect(foundEntry2.e.definitions.length).toBe(1);
            expect(foundEntry2.e.ownerId).toEqual(Identifier<Student>(1));
          }
        });
        return Effect.provide(program, layer);
      })
    })
  })
})
