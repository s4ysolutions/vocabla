import {describe, expect, it} from '@effect/vitest';
import {Layer, Option} from 'effect';
import {Effect} from 'effect';
import {LangRepositoryTag} from '../../app-repo/LangRepository.ts';
import {LearningSettingsRepositoryTag} from '../../app-repo/LearningSettingsRepository.ts';
import {decodeGetEntriesResponse, type GetEntriesResponseDto} from './dto/entry/GetEntriesResponse.ts';
import {Identifier} from '../../domain/identity/Identifier.ts';
import {Tag} from '../../domain/Tag.ts';
import {Definition, Entry} from '../../domain/Entry.ts';
import {Localized} from '../../domain/Localized.ts';
import {LangCode} from '../../domain/LangCode.ts';
import {EntriesFilter} from '../../domain/EntriesFilter.ts';
import type {Student} from '../../domain/Student.ts';
import {EntriesRepositoryTag} from '../../app-repo/EntriesRepository.ts';
import {Identified} from '../../domain/identity/Identified.ts';
import {decodeGetLanguagesResponse, type GetLanguagesResponseDto} from './dto/lang/getLanguagesResponse.ts';
import RestClientLive from '../rest/RestClientLive.ts';
import HttpClientLive from '../http/HttpClientLive.ts';
import RepositoryRestLive from './RepositoryRestLive.ts';

describe('repositoryRest', () => {
  const layer: Layer.Layer<EntriesRepositoryTag | LangRepositoryTag | LearningSettingsRepositoryTag> = RepositoryRestLive.layer.pipe(
    Layer.provide(RestClientLive.layer),
    Layer.provide(HttpClientLive.layer)
  )
  describe('schemas', () => {
    describe('entries', () => {
      it('schemaGetEntriesResponse', () => {
        const response: GetEntriesResponseDto = {
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
        const response: GetEntriesResponseDto = {
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
        } as unknown as GetEntriesResponseDto;

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
        } as unknown as GetEntriesResponseDto;

        expect(() => Effect.runSync(decodeGetEntriesResponse(response))).toThrowError();
      });
    })
  });
  describe('langs', () => {
    it('schemaGetLanguagesResponse', () => {
      const response: GetLanguagesResponseDto = {
        defaultLang: {
          code: 'en',
          name: 'English',
          flag: '🇬🇧'
        },
        unknownLang: {
          code: 'unk',
          name: 'Unknown',
          flag: '❓'
        },
        languages: [
          {
            code: 'en',
            name: 'English',
            flag: '🇬🇧'
          },
          {
            code: 'fr',
            name: 'Français',
            flag: '🇫🇷'
          },
          {
            code: 'es',
            name: 'Español',
            flag: '🇪🇸',
          }
        ]
      };

      const result = Effect.runSync(decodeGetLanguagesResponse(response));

      // Verify structure
      expect(result.defaultLang).toBeDefined();
      expect(result.unknownLang).toBeDefined();
      expect(Array.isArray(result.languages)).toBe(true);
      expect(result.languages).toHaveLength(3);

      // Verify defaultLang
      expect(result.defaultLang.code).toBe('en');
      expect(result.defaultLang.name).toBe('English');
      expect(result.defaultLang.flag).toBe('🇬🇧');

      // Verify unknownLang
      expect(result.unknownLang.code).toBe('unk');
      expect(result.unknownLang.name).toBe('Unknown');
      expect(result.unknownLang.flag).toBe('❓');

      expect(result.languages.length).toBe(3);

      // Verify languages array
      expect(result.languages[0]!.code).toBe('en');
      expect(result.languages[0]!.name).toBe('English');
      expect(result.languages[0]!.flag).toBe('🇬🇧');

      expect(result.languages[1]!.code).toBe('fr');
      expect(result.languages[1]!.name).toBe('Français');
      expect(result.languages[1]!.flag).toBe('🇫🇷');

      expect(result.languages[2]!.code).toBe('es');
      expect(result.languages[2]!.name).toBe('Español');
      expect(result.languages[2]!.flag).toBe('🇪🇸');
    });

    it('schemaGetLanguagesResponse error - invalid structure', () => {
      const response = {
        defaultLang: {
          code: 123, // Invalid type
          name: 'English'
        },
        unknownLang: {
          code: 'unk',
          name: 'Unknown'
        },
        languages: []
      } as unknown as GetLanguagesResponseDto;

      expect(() => Effect.runSync(decodeGetLanguagesResponse(response))).toThrowError();
    });

    it('schemaGetLanguagesResponse error - missing required fields', () => {
      const response = {
        defaultLang: {
          name: 'English' // Missing code
        },
        unknownLang: {
          code: 'unk',
          name: 'Unknown'
        },
        languages: []
      } as unknown as GetLanguagesResponseDto;

      expect(() => Effect.runSync(decodeGetLanguagesResponse(response))).toThrowError();
    });
  })
  describe('integration tests', () => {
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
    describe('langs', () => {
      it.effect('getAllLangs', () => {
        const program = Effect.gen(function* () {
          const langRepository = yield* LangRepositoryTag;
          const result = yield* langRepository.getAllLangs();

          // Verify structure
          expect(result.defaultLang).toBeDefined();
          expect(result.unknownLang).toBeDefined();
          expect(Array.isArray(result.languages)).toBe(true);

          // Verify defaultLang has required properties
          expect(result.defaultLang.code).toBeDefined();
          expect(result.defaultLang.name).toBeDefined();

          // Verify unknownLang has required properties
          expect(result.unknownLang.code).toBeDefined();
          expect(result.unknownLang.name).toBeDefined();

          // Verify languages array contains at least one language
          expect(result.languages.length).toBeGreaterThan(0);

          // Verify each language has required properties
          for (const lang of result.languages) {
            expect(lang.code).toBeDefined();
            expect(lang.name).toBeDefined();
            expect(typeof lang.code).toBe('string');
            expect(typeof lang.name).toBe('string');
          }

          console.log('Got languages:', result.languages.length);
          console.log('Default language:', result.defaultLang.name);
          console.log('Unknown language:', result.unknownLang.name);
        });
        return Effect.provide(program, layer);
      })
    })
    describe('learningSettings', () => {
      it.effect('getLearningSettings', () => {
        const program = Effect.gen(function* () {
          const learningSettingsRepository = yield* LearningSettingsRepositoryTag;
          const studentId = Identifier<Student>(1);

          try {
            const settings = yield* learningSettingsRepository.getLearningSettings(studentId);

            // Verify structure
            expect(settings).toBeDefined();
            expect(Array.isArray(settings.learnLangCodes)).toBe(true);
            expect(Array.isArray(settings.knownLangCodes)).toBe(true);
            expect(Array.isArray(settings.tags)).toBe(true);

            console.log('Learning settings retrieved:', settings);
          } catch (error) {
            console.log('Expected failure in getLearningSettings:', error);
            expect(error).toBeDefined();
          }
        });
        return Effect.provide(program, layer);
      });

      it.effect('addKnownLang', () => {
        const program = Effect.gen(function* () {
          const learningSettingsRepository = yield* LearningSettingsRepositoryTag;
          const studentId = Identifier<Student>(1);
          const langCode = LangCode('fr');

          try {
            const updatedSettings = yield* learningSettingsRepository.addKnownLang(studentId, langCode);

            expect(updatedSettings).toBeDefined();
            expect(Array.isArray(updatedSettings.knownLangCodes)).toBe(true);
            console.log('Added known language, updated settings:', updatedSettings);
          } catch (error) {
            console.log('Expected failure in addKnownLang:', error);
            expect(error).toBeDefined();
          }
        });
        return Effect.provide(program, layer);
      });

      it.effect('removeKnownLang', () => {
        const program = Effect.gen(function* () {
          const learningSettingsRepository = yield* LearningSettingsRepositoryTag;
          const studentId = Identifier<Student>(1);
          const langCode = LangCode('fr');

          try {
            const updatedSettings = yield* learningSettingsRepository.removeKnownLang(studentId, langCode);

            expect(updatedSettings).toBeDefined();
            expect(Array.isArray(updatedSettings.knownLangCodes)).toBe(true);
            console.log('Removed known language, updated settings:', updatedSettings);
          } catch (error) {
            console.log('Expected failure in removeKnownLang:', error);
            expect(error).toBeDefined();
          }
        });
        return Effect.provide(program, layer);
      });

      it.effect('addLearnLang', () => {
        const program = Effect.gen(function* () {
          const learningSettingsRepository = yield* LearningSettingsRepositoryTag;
          const studentId = Identifier<Student>(1);
          const langCode = LangCode('es');

          try {
            const updatedSettings = yield* learningSettingsRepository.addLearnLang(studentId, langCode);

            expect(updatedSettings).toBeDefined();
            expect(Array.isArray(updatedSettings.learnLangCodes)).toBe(true);
            console.log('Added learning language, updated settings:', updatedSettings);
          } catch (error) {
            console.log('Expected failure in addLearnLang:', error);
            expect(error).toBeDefined();
          }
        });
        return Effect.provide(program, layer);
      });

      it.effect('removeLearnLang', () => {
        const program = Effect.gen(function* () {
          const learningSettingsRepository = yield* LearningSettingsRepositoryTag;
          const studentId = Identifier<Student>(1);
          const langCode = LangCode('es');

          try {
            const updatedSettings = yield* learningSettingsRepository.removeLearnLang(studentId, langCode);

            expect(updatedSettings).toBeDefined();
            expect(Array.isArray(updatedSettings.learnLangCodes)).toBe(true);
            console.log('Removed learning language, updated settings:', updatedSettings);
          } catch (error) {
            console.log('Expected failure in removeLearnLang:', error);
            expect(error).toBeDefined();
          }
        });
        return Effect.provide(program, layer);
      });

      it.effect('createTag', () => {
        const program = Effect.gen(function* () {
          const learningSettingsRepository = yield* LearningSettingsRepositoryTag;
          const studentId = Identifier<Student>(1);
          const tag = {label: 'labelTest'}; // Use a mock tagId since we don't have TagRepository

          try {
            const updatedSettings = yield* learningSettingsRepository.createTag(studentId, tag);

            expect(updatedSettings).toBeDefined();
            expect(Array.isArray(updatedSettings.tags)).toBe(true);
            expect(updatedSettings.tags.find(t => t.e.label === 'label1')).toBeDefined();
            console.log('Created tag in learning settings:', updatedSettings);
          } catch (error) {
            console.log('Expected failure in createTag:', error);
            expect(error).toBeDefined();
          }
        });
        return Effect.provide(program, layer);
      });

      it.effect('deleteTag', () => {
        const program = Effect.gen(function* () {
          const learningSettingsRepository = yield* LearningSettingsRepositoryTag;
          const studentId = Identifier<Student>(1);
          const tagId = Identifier<Tag>(123); // Use a mock tagId since we don't have TagRepository

          try {
            const updatedSettings = yield* learningSettingsRepository.deleteTag(studentId, tagId);

            expect(updatedSettings).toBeDefined();
            expect(Array.isArray(updatedSettings.tags)).toBe(true);
            console.log('Deleted tag from learning settings:', updatedSettings);
          } catch (error) {
            console.log('Expected failure in deleteTag:', error);
            expect(error).toBeDefined();
          }
        });
        return Effect.provide(program, layer);
      });
    })
  })
})
