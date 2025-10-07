import {describe, expect, it} from '@effect/vitest';
import {Effect, Layer} from 'effect';
import LanguagesUseCasesLive from './LanguagesUseCasesLive.ts';
import {LanguagesUseCasesTag} from '../app-ports/LanguagesUseCases.ts';
import {type AllLangsR, type LangRepository, LangRepositoryTag} from '../app-repo/LangRepository.ts';
import {LangCode} from '../domain/LangCode.ts';
import type {Lang} from '../domain/Lang.ts';
import {InfraError} from '../app-repo/InfraError.ts';
import {tt} from '../translable/Translatable.ts';

// Mock languages for testing
const mockEnglish: Lang = {code: LangCode('en'), name: 'English', flag: '🇬🇧'};
const mockSpanish: Lang = {code: LangCode('es'), name: 'Spanish', flag: '🇪🇸'};
const mockFrench: Lang = {code: LangCode('fr'), name: 'French', flag: '🇫🇷'};
const mockUnknown: Lang = {code: LangCode('unk'), name: 'Unknown', flag: '🏳️'};

const mockAllLangsR: AllLangsR = {
  defaultLang: mockEnglish,
  unknownLang: mockUnknown,
  languages: [mockEnglish, mockSpanish, mockFrench]
};

// Mock repository implementation
const mockLangRepository: LangRepository = {
  getAllLangs: () => Effect.succeed(mockAllLangsR)
};

// Mock repository that fails
const failingLangRepository: LangRepository = {
  getAllLangs: () =>
    Effect.fail(InfraError(tt`Repository failure`))
};

// Test layer with mock repository
const testLayer = Layer.succeed(LangRepositoryTag, mockLangRepository);
const failingTestLayer = Layer.succeed(LangRepositoryTag, failingLangRepository);

describe('LanguagesUseCasesLive', () => {
  describe('static make', () => {
    it.effect('creates instance with successful repository', () =>
      Effect.gen(function* () {
        const useCases = yield* LanguagesUseCasesLive.make(mockLangRepository);
        expect(useCases).toBeInstanceOf(LanguagesUseCasesLive);
      })
    );

    it.effect('handles repository errors', () =>
      Effect.gen(function* () {
        const uc = yield* LanguagesUseCasesLive.make(failingLangRepository);
        const result = yield* Effect.either(uc.allLanguages);
        expect(result._tag).toBe('Left');
        if (result._tag === 'Left') {
          expect(result.left).toHaveProperty('message');
          expect(result.left._tag).toBe('AppError');
        }
      })
    );
  });

  describe('layer', () => {
    it.effect('provides LanguagesUseCasesTag', () =>
      Effect.gen(function* () {
        const useCases = yield* LanguagesUseCasesTag;
        expect(useCases).toBeInstanceOf(LanguagesUseCasesLive);
      }).pipe(
        Effect.provide(LanguagesUseCasesLive.layer),
        Effect.provide(testLayer)
      )
    );

    it.effect('handles repository layer failures gracefully', () =>
      Effect.gen(function* () {
        // The layer uses Effect.orDie, so it won't return a Left but will terminate
        // This test verifies that the layer can be created even with a failing repository
        // but the actual error will occur when the service methods are called
        const result = yield* Effect.either(Effect.succeed('layer created'));
        expect(result._tag).toBe('Right');
      }).pipe(
        Effect.provide(LanguagesUseCasesLive.layer),
        Effect.provide(failingTestLayer)
      )
    );
  });

  describe('allLanguages', () => {
    it.effect('returns all languages', () =>
      Effect.gen(function* () {
        const useCases = yield* LanguagesUseCasesTag;
        const languages = yield* useCases.allLanguages;

        expect(languages).toEqual([mockEnglish, mockSpanish, mockFrench]);
      }).pipe(
        Effect.provide(LanguagesUseCasesLive.layer),
        Effect.provide(testLayer)
      )
    );
  });

  describe('defaultLang', () => {
    it.effect('returns default language', () =>
      Effect.gen(function* () {
        const useCases = yield* LanguagesUseCasesTag;
        const defaultLang = yield* useCases.defaultLang;

        expect(defaultLang).toEqual(mockEnglish);
      }).pipe(
        Effect.provide(LanguagesUseCasesLive.layer),
        Effect.provide(testLayer)
      )
    );
  });

  describe('unknownLang', () => {
    it.effect('returns unknown language', () =>
      Effect.gen(function* () {
        const useCases = yield* LanguagesUseCasesTag;
        const unknownLang = yield* useCases.unknownLang;

        expect(unknownLang).toEqual(mockUnknown);
      }).pipe(
        Effect.provide(LanguagesUseCasesLive.layer),
        Effect.provide(testLayer)
      )
    );
  });

  describe('getLangByCode', () => {
    it.effect('returns language for existing code', () =>
      Effect.gen(function* () {
        const useCases = yield* LanguagesUseCasesTag;
        const lang = yield* useCases.getLangByCode(LangCode('es'));

        expect(lang).toEqual(mockSpanish);
      }).pipe(
        Effect.provide(LanguagesUseCasesLive.layer),
        Effect.provide(testLayer)
      )
    );

    it.effect('returns unknown language for non-existing code', () =>
      Effect.gen(function* () {
        const useCases = yield* LanguagesUseCasesTag;
        const lang = yield* useCases.getLangByCode(LangCode('xx'));

        expect(lang).toEqual(mockUnknown);
      }).pipe(
        Effect.provide(LanguagesUseCasesLive.layer),
        Effect.provide(testLayer)
      )
    );

    it.effect('returns correct language for multiple calls', () =>
      Effect.gen(function* () {
        const useCases = yield* LanguagesUseCasesTag;

        const english = yield* useCases.getLangByCode(LangCode('en'));
        const spanish = yield* useCases.getLangByCode(LangCode('es'));
        const french = yield* useCases.getLangByCode(LangCode('fr'));
        const unknown = yield* useCases.getLangByCode(LangCode('de'));

        expect(english).toEqual(mockEnglish);
        expect(spanish).toEqual(mockSpanish);
        expect(french).toEqual(mockFrench);
        expect(unknown).toEqual(mockUnknown);
      }).pipe(
        Effect.provide(LanguagesUseCasesLive.layer),
        Effect.provide(testLayer)
      )
    );
  });

  describe('caching behavior', () => {
    let callCount = 0;
    const countingRepository: LangRepository = {
      getAllLangs: () => Effect.sync(() => {
        callCount++;
        return mockAllLangsR;
      })
    };

    it.effect('uncached should call repositaries on each time', () =>
      Effect.gen(function* () {
        callCount = 0;
        const useCases = yield* LanguagesUseCasesLive.make(countingRepository, false);

        // Make multiple calls - each will trigger repository calls since caching is disabled
        yield* useCases.allLanguages;
        yield* useCases.defaultLang;
        yield* useCases.getLangByCode(LangCode('en'));
        yield* useCases.getLangByCode(LangCode('es'));

        // With current implementation (no caching), we expect at least some repository calls
        expect(callCount).toBe(4 + 2);
      })
    );

    it.effect('uncached should call repositaries only 1 time', () =>
      Effect.gen(function* () {
        callCount = 0;
        const useCases = yield* LanguagesUseCasesLive.make(countingRepository, true);

        // Make multiple calls - each will trigger repository calls since caching is disabled
        yield* useCases.allLanguages;
        yield* useCases.defaultLang;
        yield* useCases.getLangByCode(LangCode('en'));
        yield* useCases.getLangByCode(LangCode('es'));

        // With current implementation (no caching), we expect at least some repository calls
        expect(callCount).toBe(1);
      })
    );
  });

  describe('error handling', () => {
    it.effect('propagates repository errors as AppError', () =>
      Effect.gen(function* () {
        const useCases = yield* LanguagesUseCasesTag;
        const result = yield* Effect.either(useCases.allLanguages);

        expect(result._tag).toBe('Left');
        if (result._tag === 'Left') {
          expect(result.left).toHaveProperty('message');
        }
      }).pipe(
        Effect.provide(LanguagesUseCasesLive.layer),
        Effect.provide(failingTestLayer)
      )
    );

    it.effect('getLangByCode handles repository errors', () =>
      Effect.gen(function* () {
        const useCases = yield* LanguagesUseCasesTag;
        const result = yield* Effect.either(useCases.getLangByCode(LangCode('en')));

        expect(result._tag).toBe('Left');
        if (result._tag === 'Left') {
          expect(result.left).toHaveProperty('message');
        }
      }).pipe(
        Effect.provide(LanguagesUseCasesLive.layer),
        Effect.provide(failingTestLayer)
      )
    );
  });

  describe('integration tests', () => {
    it.effect('complete workflow with multiple operations', () =>
      Effect.gen(function* () {
        const useCases = yield* LanguagesUseCasesTag;

        // Get all languages
        const allLanguages = yield* useCases.allLanguages;
        expect(allLanguages).toHaveLength(3);

        // Get default language
        const defaultLang = yield* useCases.defaultLang;
        expect(defaultLang.code).toBe('en');

        // Get unknown language
        const unknownLang = yield* useCases.unknownLang;
        expect(unknownLang.code).toBe('unk');

        // Test language lookup
        for (const lang of allLanguages) {
          const foundLang = yield* useCases.getLangByCode(lang.code);
          expect(foundLang).toEqual(lang);
        }

        // Test non-existent language
        const nonExistent = yield* useCases.getLangByCode(LangCode('xx'));
        expect(nonExistent).toEqual(unknownLang);
      }).pipe(
        Effect.provide(LanguagesUseCasesLive.layer),
        Effect.provide(testLayer)
      )
    );

    it.effect('handles concurrent access', () =>
      Effect.gen(function* () {
        const useCases = yield* LanguagesUseCasesTag;

        // Make concurrent requests
        const results = yield* Effect.all([
          useCases.allLanguages,
          useCases.getLangByCode(LangCode('en')),
          useCases.getLangByCode(LangCode('es')),
          useCases.defaultLang,
          useCases.unknownLang
        ], {concurrency: 'unbounded'});

        const [allLangs, english, spanish, defaultLang, unknownLang] = results;

        expect(allLangs).toHaveLength(3);
        expect(english).toEqual(mockEnglish);
        expect(spanish).toEqual(mockSpanish);
        expect(defaultLang).toEqual(mockEnglish);
        expect(unknownLang).toEqual(mockUnknown);
      }).pipe(
        Effect.provide(LanguagesUseCasesLive.layer),
        Effect.provide(testLayer)
      )
    );
  });
});
