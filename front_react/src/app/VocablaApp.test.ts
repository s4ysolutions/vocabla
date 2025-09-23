import {describe, expect, it} from '@effect/vitest';
import {Layer} from 'effect';
import {Effect} from 'effect';
import {vocablaAppLayer} from './VocablaApp.ts';
import {GetDefaultLangUseCaseTag} from '../app-ports/languages/GetDefaultLangUseCase.ts';
import {GetLangByCodeUseCaseTag} from '../app-ports/languages/GetLangByCodeUseCase.ts';
import {LangCode} from '../domain/LangCode.ts';
import httpClientLive from '../infra/http/httpClientLive.ts';
import {restClientLayer} from '../infra/rest/restClientLive.ts';
import {repositoryRestLayer} from '../infra/repo/repositoryRestLive.ts';

describe('VocablaApp', () => {
  const layer = vocablaAppLayer.pipe(
    Layer.provide(repositoryRestLayer),
    Layer.provide(restClientLayer),
    Layer.provide(httpClientLive)
  );

  describe('integration tests', () => {
    it.effect('vocablaAppLayer provides default language "en"', () => {
      const program = Effect.gen(function* () {
        const defaultLangUseCase = yield* GetDefaultLangUseCaseTag;
        const defaultLang = defaultLangUseCase.defaultLang;

        expect(defaultLang).toBeDefined();
        expect(defaultLang.code).toBe('en');
        expect(defaultLang.name).toBeDefined();
        expect(typeof defaultLang.name).toBe('string');

        console.log('Default language:', defaultLang.name, `(${defaultLang.code})`);
      });
      return Effect.provide(program, layer);
    });

    it.effect('vocablaAppLayer can getLangByCode for "es"', () => {
      const program = Effect.gen(function* () {
        const langByCodeUseCase = yield* GetLangByCodeUseCaseTag;
        const esLang = langByCodeUseCase.getLangByCode(LangCode('es'));

        expect(esLang).toBeDefined();
        expect(esLang.code).toBe('es');
        expect(esLang.name).toBeDefined();
        expect(typeof esLang.name).toBe('string');

        console.log('Spanish language:', esLang.name, `(${esLang.code})`);
      });
      return Effect.provide(program, layer);
    });

    it.effect('vocablaAppLayer provides consistent language data', () => {
      const program = Effect.gen(function* () {
        const defaultLangUseCase = yield* GetDefaultLangUseCaseTag;
        const langByCodeUseCase = yield* GetLangByCodeUseCaseTag;

        const defaultLang = defaultLangUseCase.defaultLang;
        const enLangByCode = langByCodeUseCase.getLangByCode(LangCode('en'));

        // Verify that getting 'en' by code returns the same as default language
        expect(enLangByCode.code).toBe(defaultLang.code);
        expect(enLangByCode.name).toBe(defaultLang.name);

        // Test unknown language code fallback
        const unknownLang = langByCodeUseCase.getLangByCode(LangCode('xyz'));
        expect(unknownLang).toBeDefined();
        expect(unknownLang.code).toBeDefined();

        console.log('Default language matches en by code:', defaultLang.code === enLangByCode.code);
        console.log('Unknown language fallback:', unknownLang.name, `(${unknownLang.code})`);
      });
      return Effect.provide(program, layer);
    });

    it.effect('vocablaAppLayer handles multiple language codes', () => {
      const program = Effect.gen(function* () {
        const langByCodeUseCase = yield* GetLangByCodeUseCaseTag;

        const testCodes = ['en', 'es', 'fr', 'de'] as const;
        const languages = testCodes.map(code => ({
          code,
          lang: langByCodeUseCase.getLangByCode(LangCode(code))
        }));

        // Verify all languages are returned (even if some fall back to unknown)
        for (const {code, lang} of languages) {
          expect(lang).toBeDefined();
          expect(lang.code).toBeDefined();
          expect(lang.name).toBeDefined();
          expect(typeof lang.code).toBe('string');
          expect(typeof lang.name).toBe('string');

          console.log(`Language ${code}:`, lang.name, `(${lang.code})`);
        }

        // Verify English is available
        const enLang = languages.find(l => l.code === 'en')?.lang;
        expect(enLang?.code).toBe('en');

        // Verify Spanish is available
        const esLang = languages.find(l => l.code === 'es')?.lang;
        expect(esLang?.code).toBe('es');
      });
      return Effect.provide(program, layer);
    });
  });
});
