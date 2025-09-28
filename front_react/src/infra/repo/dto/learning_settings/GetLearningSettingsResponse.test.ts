import {describe, expect, it} from '@effect/vitest';
import {Effect} from 'effect';
import {decodeGetLearningSettingsResponse, type GetLearningSettingsResponseDto} from './GetLearningSettingsResponse.ts';
import {LangCode} from '../../../../domain/LangCode.ts';
import {Identified} from '../../../../domain/identity/Identified.ts';
import type {TagSmall} from '../../../../domain/TagSmall.ts';

describe('decodeGetLearningSettingsResponse', () => {
  it('should decode valid response with all fields', () => {
    const response: GetLearningSettingsResponseDto = {
      learningSettings: {
        learnLanguages: ['en', 'es', 'fr'],
        knownLanguages: ['de', 'it'],
        tags: [
          {id: 1, e: {label: 'tag1'}},
          {id: 5, e: {label: 'tag5'}},
          {id: 10, e: {label: 'tag10'}}
        ]
      }
    };

    const result = Effect.runSync(decodeGetLearningSettingsResponse(response));

    expect(result).toEqual({
      learnLangCodes: [LangCode('en'), LangCode('es'), LangCode('fr')],
      knownLangCodes: [LangCode('de'), LangCode('it')],
      tags: [
        Identified<TagSmall>(1, {label: 'tag1'}),
        Identified<TagSmall>(5, {label: 'tag5'}),
        Identified<TagSmall>(10, {label: 'tag10'})
      ]
    });
  });

  it('should decode response with empty arrays', () => {
    const response: GetLearningSettingsResponseDto = {
      learningSettings: {
        learnLanguages: [],
        knownLanguages: [],
        tags: []
      }
    };

    const result = Effect.runSync(decodeGetLearningSettingsResponse(response));

    expect(result).toEqual({
      learnLangCodes: [],
      knownLangCodes: [],
      tags: []
    });
  });

  it('should decode response with only learning languages', () => {
    const response: GetLearningSettingsResponseDto = {
      learningSettings: {
        learnLanguages: ['ja', 'ko'],
        knownLanguages: [],
        tags: []
      }
    };

    const result = Effect.runSync(decodeGetLearningSettingsResponse(response));

    expect(result).toEqual({
      learnLangCodes: [LangCode('ja'), LangCode('ko')],
      knownLangCodes: [],
      tags: []
    });
  });

  it('should decode response with only known languages', () => {
    const response: GetLearningSettingsResponseDto = {
      learningSettings: {
        learnLanguages: [],
        knownLanguages: ['zh', 'ar'],
        tags: []
      }
    };

    const result = Effect.runSync(decodeGetLearningSettingsResponse(response));

    expect(result).toEqual({
      learnLangCodes: [],
      knownLangCodes: [LangCode('zh'), LangCode('ar')],
      tags: []
    });
  });

  it('should decode response with only tags', () => {
    const response: GetLearningSettingsResponseDto = {
      learningSettings: {
        learnLanguages: [],
        knownLanguages: [],
        tags: [
          {id: 42, e: {label: 'work'}},
          {id: 99, e: {label: 'travel'}}
        ]
      }
    };

    const result = Effect.runSync(decodeGetLearningSettingsResponse(response));

    expect(result).toEqual({
      learnLangCodes: [],
      knownLangCodes: [],
      tags: [
        Identified<TagSmall>(42, {label: 'work'}),
        Identified<TagSmall>(99, {label: 'travel'})
      ]
    });
  });

  it('should decode response with single items in each array', () => {
    const response: GetLearningSettingsResponseDto = {
      learningSettings: {
        learnLanguages: ['pt'],
        knownLanguages: ['ru'],
        tags: [{id: 7, e: {label: 'important'}}]
      }
    };

    const result = Effect.runSync(decodeGetLearningSettingsResponse(response));

    expect(result).toEqual({
      learnLangCodes: [LangCode('pt')],
      knownLangCodes: [LangCode('ru')],
      tags: [Identified<TagSmall>(7, {label: 'important'})]
    });
  });

  it('should fail when learningSettings is missing', () => {
    const response = {} as GetLearningSettingsResponseDto;

    expect(() => Effect.runSync(decodeGetLearningSettingsResponse(response))).toThrowError();
  });

  it('should fail when learnLanguages is missing', () => {
    const response = {
      learningSettings: {
        knownLanguages: ['de'],
        tags: []
      }
    } as unknown as GetLearningSettingsResponseDto;

    expect(() => Effect.runSync(decodeGetLearningSettingsResponse(response))).toThrowError();
  });

  it('should fail when knownLanguages is missing', () => {
    const response = {
      learningSettings: {
        learnLanguages: ['en'],
        tags: []
      }
    } as unknown as GetLearningSettingsResponseDto;

    expect(() => Effect.runSync(decodeGetLearningSettingsResponse(response))).toThrowError();
  });

  it('should fail when tags is missing', () => {
    const response = {
      learningSettings: {
        learnLanguages: ['en'],
        knownLanguages: ['de']
      }
    } as unknown as GetLearningSettingsResponseDto;

    expect(() => Effect.runSync(decodeGetLearningSettingsResponse(response))).toThrowError();
  });

  it('should fail when learnLanguages contains non-string values', () => {
    const response = {
      learningSettings: {
        learnLanguages: [123, 'en'],
        knownLanguages: ['de'],
        tags: []
      }
    } as unknown as GetLearningSettingsResponseDto;

    expect(() => Effect.runSync(decodeGetLearningSettingsResponse(response))).toThrowError();
  });

  it('should fail when knownLanguages contains non-string values', () => {
    const response = {
      learningSettings: {
        learnLanguages: ['en'],
        knownLanguages: [null, 'de'],
        tags: []
      }
    } as unknown as GetLearningSettingsResponseDto;

    expect(() => Effect.runSync(decodeGetLearningSettingsResponse(response))).toThrowError();
  });

  it('should fail when tags array contains invalid tag structure', () => {
    const response = {
      learningSettings: {
        learnLanguages: ['en'],
        knownLanguages: ['de'],
        tags: [
          {id: 'not-a-number', e: {label: 'test'}}
        ]
      }
    } as unknown as GetLearningSettingsResponseDto;

    expect(() => Effect.runSync(decodeGetLearningSettingsResponse(response))).toThrowError();
  });

  it('should fail when tag is missing id property', () => {
    const response = {
      learningSettings: {
        learnLanguages: ['en'],
        knownLanguages: ['de'],
        tags: [
          {e: {label: 'test'}} // Missing id
        ]
      }
    } as unknown as GetLearningSettingsResponseDto;

    expect(() => Effect.runSync(decodeGetLearningSettingsResponse(response))).toThrowError();
  });

  it('should fail when tag is missing e property', () => {
    const response = {
      learningSettings: {
        learnLanguages: ['en'],
        knownLanguages: ['de'],
        tags: [
          {id: 1} // Missing e
        ]
      }
    } as unknown as GetLearningSettingsResponseDto;

    expect(() => Effect.runSync(decodeGetLearningSettingsResponse(response))).toThrowError();
  });

  it('should fail when tag e property is missing label', () => {
    const response = {
      learningSettings: {
        learnLanguages: ['en'],
        knownLanguages: ['de'],
        tags: [
          {id: 1, e: {}} // Missing label in e
        ]
      }
    } as unknown as GetLearningSettingsResponseDto;

    expect(() => Effect.runSync(decodeGetLearningSettingsResponse(response))).toThrowError();
  });

  it('should fail when tag label is not a string', () => {
    const response = {
      learningSettings: {
        learnLanguages: ['en'],
        knownLanguages: ['de'],
        tags: [
          {id: 1, e: {label: 123}} // Invalid label type
        ]
      }
    } as unknown as GetLearningSettingsResponseDto;

    expect(() => Effect.runSync(decodeGetLearningSettingsResponse(response))).toThrowError();
  });

  it('should handle complex realistic response', () => {
    const response: GetLearningSettingsResponseDto = {
      learningSettings: {
        learnLanguages: ['es', 'fr', 'de', 'ja'],
        knownLanguages: ['en', 'it'],
        tags: [
          {id: 1, e: {label: 'grammar'}},
          {id: 12, e: {label: 'vocabulary'}},
          {id: 33, e: {label: 'business'}},
          {id: 101, e: {label: 'travel phrases'}},
          {id: 999, e: {label: 'idioms'}}
        ]
      }
    };

    const result = Effect.runSync(decodeGetLearningSettingsResponse(response));

    expect(result.learnLangCodes).toHaveLength(4);
    expect(result.knownLangCodes).toHaveLength(2);
    expect(result.tags).toHaveLength(5);

    expect(result.learnLangCodes).toEqual([
      LangCode('es'), LangCode('fr'), LangCode('de'), LangCode('ja')
    ]);
    expect(result.knownLangCodes).toEqual([
      LangCode('en'), LangCode('it')
    ]);
    expect(result.tags).toEqual([
      Identified<TagSmall>(1, {label: 'grammar'}),
      Identified<TagSmall>(12, {label: 'vocabulary'}),
      Identified<TagSmall>(33, {label: 'business'}),
      Identified<TagSmall>(101, {label: 'travel phrases'}),
      Identified<TagSmall>(999, {label: 'idioms'})
    ]);
  });
  it('{"learningSettings":{"learnLanguages":[],"knownLanguages":["en","fr"],"tags":[]}}', () => {
    const response: GetLearningSettingsResponseDto =
      JSON.parse('{"learningSettings":{"learnLanguages":[],"knownLanguages":["string","fr"],"tags":[]}}');

    const result = Effect.runSync(decodeGetLearningSettingsResponse(response));
    expect(result).toEqual({
      learnLangCodes: [],
      knownLangCodes: [LangCode('en'),
        LangCode('fr')], tags: []
    })
  })
});
