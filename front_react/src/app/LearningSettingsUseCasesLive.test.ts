import {describe, expect, it} from '@effect/vitest';
import {Effect} from 'effect';
import LearningSettingsUseCasesLive from './LearningSettingsUseCasesLive.ts';
import {type LearningSettingsRepository} from '../app-repo/LearningSettingsRepository.ts';
import {type MeUseCases} from '../app-ports/MeUseCases.ts';
import {type LanguagesUseCases} from '../app-ports/LanguagesUseCases.ts';
import {LangCode} from '../domain/LangCode.ts';
import type {Lang} from '../domain/Lang.ts';
import {Identifier} from '../domain/identity/Identifier.ts';
import type {Student, StudentId} from '../domain/Student.ts';
import {SuccessData} from '../app-ports/types.ts';

describe('LearningSettingsUseCasesLive', () => {
  describe('refreshLearningSettings', () => {
    it.effect('method exists and returns Effect', () =>
      Effect.gen(function* () {
        // Minimal mocks
        const mockStudentId: StudentId = Identifier<Student>(1);
        const mockEnglish: Lang = {code: LangCode('en'), name: 'English', flag: '🇬🇧'};

        const mockRepository: LearningSettingsRepository = {
          getLearningSettings: () => Effect.succeed({
            learnLangCodes: [],
            knownLangCodes: [],
            tags: []
          }),
          addKnownLang: () => Effect.succeed({learnLangCodes: [], knownLangCodes: [], tags: []}),
          removeKnownLang: () => Effect.succeed({learnLangCodes: [], knownLangCodes: [], tags: []}),
          addLearnLang: () => Effect.succeed({learnLangCodes: [], knownLangCodes: [], tags: []}),
          removeLearnLang: () => Effect.succeed({learnLangCodes: [], knownLangCodes: [], tags: []}),
          createTag: () => Effect.succeed({learnLangCodes: [], knownLangCodes: [], tags: []}),
          deleteTag: () => Effect.succeed({learnLangCodes: [], knownLangCodes: [], tags: []})
        };

        const mockMeUseCases: MeUseCases = {
          currentStudentId: Effect.succeed(SuccessData(mockStudentId))
        };

        const mockLanguagesUseCases: LanguagesUseCases = {
          allLanguages: Effect.succeed([mockEnglish]),
          defaultLang: Effect.succeed(mockEnglish),
          unknownLang: Effect.succeed(mockEnglish),
          getLangByCode: () => Effect.succeed(mockEnglish)
        };

        // Create instance
        const useCases = yield* LearningSettingsUseCasesLive.make(
          mockRepository,
          mockMeUseCases,
          mockLanguagesUseCases
        );

        // Just verify the method exists and is a function
        expect(typeof useCases.refreshLearningSettings).toBe('function');

        // Verify it returns an Effect (object)
        const result = yield * useCases.refreshLearningSettings();
        expect(result).toEqual({learnLangs: [], knownLangs: [], tags: []});
      })
    );
  });
});
