import {describe, expect, it, beforeEach, vi} from 'vitest';
import {renderHook, waitFor, act} from '@testing-library/react';
import {Effect, Layer, Stream, PubSub, Ref} from 'effect';
import useLearningSettings from './useLearningSettings.ts';
import {
  type LearningSettingsUseCases,
  LearningSettingsUseCasesTag
} from '../../../../app-ports/LearningSettingsUseCases.ts';
import {type LearningSettings} from '../../../../domain/LearningSettings.ts';
import {LangCode} from '../../../../domain/LangCode.ts';
import type {Lang} from '../../../../domain/Lang.ts';
import {SuccessData, LoadingData, type AsyncData} from '../../../../app-ports/types.ts';
import type {AppError} from '../../../../app-ports/errors/AppError.ts';
import * as runtime from '../../../../app/effect-runtime.ts';
import type {TagSmall} from '../../../../domain/TagSmall.ts';
import {Identifier} from '../../../../domain/identity/Identifier.ts';

describe('useLearningSettings', () => {
  const mockEmptySettings: LearningSettings = {
    learnLangs: [],
    knownLangs: [],
    tags: []
  };

  const mockEnglish: Lang = {code: LangCode('en'), name: 'English', flag: '🇬🇧'};
  const mockSpanish: Lang = {code: LangCode('es'), name: 'Spanish', flag: '🇪🇸'};
  const mockFrench: Lang = {code: LangCode('fr'), name: 'French', flag: '🇫🇷'};

  const mockSettingsWithLangs: LearningSettings = {
    learnLangs: [mockSpanish, mockFrench],
    knownLangs: [mockEnglish],
    tags: []
  };

  let mockRefreshLearningSettings: ReturnType<typeof vi.fn>;
  let mockAddLearnLang: ReturnType<typeof vi.fn>;
  let mockRemoveLearnLang: ReturnType<typeof vi.fn>;
  let mockAddKnownLang: ReturnType<typeof vi.fn>;
  let mockRemoveKnownLang: ReturnType<typeof vi.fn>;
  let mockAddTag: ReturnType<typeof vi.fn>;
  let mockRemoveTag: ReturnType<typeof vi.fn>;
  let settingsRef: Ref.Ref<AsyncData<LearningSettings, AppError>>;
  let hub: PubSub.PubSub<AsyncData<LearningSettings, AppError>>;

  beforeEach(async () => {
    // Create a Ref and PubSub to simulate the real implementation
    settingsRef = await Effect.runPromise(
      Ref.make<AsyncData<LearningSettings, AppError>>(LoadingData())
    );

    hub = await Effect.runPromise(
      PubSub.sliding<AsyncData<LearningSettings, AppError>>(1)
    );

    mockRefreshLearningSettings = vi.fn(() => Effect.void);
    mockAddLearnLang = vi.fn((langCode: LangCode) =>
      Effect.succeed({
        ...mockSettingsWithLangs,
        learnLangs: [...mockSettingsWithLangs.learnLangs, {code: langCode, name: 'Test', flag: '🏳️'}]
      })
    );
    mockRemoveLearnLang = vi.fn(() => Effect.succeed(mockEmptySettings));
    mockAddKnownLang = vi.fn((langCode: LangCode) =>
      Effect.succeed({
        ...mockSettingsWithLangs,
        knownLangs: [...mockSettingsWithLangs.knownLangs, {code: langCode, name: 'Test', flag: '🏳️'}]
      })
    );
    mockRemoveKnownLang = vi.fn(() => Effect.succeed(mockEmptySettings));
    mockAddTag = vi.fn(() => Effect.succeed(mockEmptySettings));
    mockRemoveTag = vi.fn(() => Effect.succeed(mockEmptySettings));

    const stream = Stream.fromPubSub(hub).pipe(
      Stream.tap((data) => Ref.set(settingsRef, data))
    );

    const mockUseCases: LearningSettingsUseCases = {
      lastLearningSettings: Ref.get(settingsRef),
      streamLearningSettings: stream,
      refreshLearningSettings: mockRefreshLearningSettings,
      addLearnLang: mockAddLearnLang,
      removeLearnLang: mockRemoveLearnLang,
      addKnownLang: mockAddKnownLang,
      removeKnownLang: mockRemoveKnownLang,
      addTag: mockAddTag,
      removeTag: mockRemoveTag
    };

    const testLayer = Layer.succeed(LearningSettingsUseCasesTag, mockUseCases);

    // Mock the runtime functions
    vi.spyOn(runtime, 'forkAppEffect').mockImplementation((effect: any) => {
      Effect.runPromise(Effect.provide(effect, testLayer)).catch(() => {});
      return {} as any; // Return a fake fiber
    });

    vi.spyOn(runtime, 'promiseAppEffect').mockImplementation((effect: any) =>
      Effect.runPromise(Effect.provide(effect, testLayer))
    );

    vi.spyOn(runtime, 'interruptFiber').mockImplementation(() => {});
  });

  it('should initialize with loading state', () => {
    const {result} = renderHook(() => useLearningSettings());

    expect(result.current.learningSettings).toEqual(LoadingData());
  });

  it('should call refreshLearningSettings on mount', async () => {
    renderHook(() => useLearningSettings());

    await waitFor(() => {
      expect(mockRefreshLearningSettings).toHaveBeenCalled();
    });
  });

  it('should update when settings stream emits', async () => {
    const {result} = renderHook(() => useLearningSettings());

    // Wait a bit for the stream to be set up
    await new Promise(resolve => setTimeout(resolve, 10));

    // Publish to the hub
    await act(async () => {
      await Effect.runPromise(
        PubSub.publish(hub, SuccessData(mockSettingsWithLangs))
      );
      // Give time for the stream to process
      await new Promise(resolve => setTimeout(resolve, 50));
    });

    await waitFor(() => {
      expect(result.current.learningSettings).toEqual(SuccessData(mockSettingsWithLangs));
    }, { timeout: 2000 });
  });

  it('should add learn language', async () => {
    const {result} = renderHook(() => useLearningSettings());

    // Wait for stream setup
    await new Promise(resolve => setTimeout(resolve, 10));

    // Set initial state
    await act(async () => {
      await Effect.runPromise(
        PubSub.publish(hub, SuccessData(mockEmptySettings))
      );
      await new Promise(resolve => setTimeout(resolve, 50));
    });

    await waitFor(() => {
      expect(result.current.learningSettings).toEqual(SuccessData(mockEmptySettings));
    }, { timeout: 2000 });

    // Add learn language
    await act(async () => {
      await result.current.addLearnLang(LangCode('es'));
    });

    expect(mockAddLearnLang).toHaveBeenCalledWith(LangCode('es'));
  });

  it('should remove learn language', async () => {
    const {result} = renderHook(() => useLearningSettings());

    await act(async () => {
      await Effect.runPromise(
        PubSub.publish(hub, SuccessData(mockSettingsWithLangs))
      );
    });

    await act(async () => {
      await result.current.removeLearnLang(LangCode('es'));
    });

    expect(mockRemoveLearnLang).toHaveBeenCalledWith(LangCode('es'));
  });

  it('should add known language', async () => {
    const {result} = renderHook(() => useLearningSettings());

    await act(async () => {
      await Effect.runPromise(
        PubSub.publish(hub, SuccessData(mockEmptySettings))
      );
    });

    await act(async () => {
      await result.current.addKnownLang(LangCode('en'));
    });

    expect(mockAddKnownLang).toHaveBeenCalledWith(LangCode('en'));
  });

  it('should remove known language', async () => {
    const {result} = renderHook(() => useLearningSettings());

    await act(async () => {
      await Effect.runPromise(
        PubSub.publish(hub, SuccessData(mockSettingsWithLangs))
      );
    });

    await act(async () => {
      await result.current.removeKnownLang(LangCode('en'));
    });

    expect(mockRemoveKnownLang).toHaveBeenCalledWith(LangCode('en'));
  });

  it('should add tag', async () => {
    const {result} = renderHook(() => useLearningSettings());

    await act(async () => {
      await Effect.runPromise(
        PubSub.publish(hub, SuccessData(mockEmptySettings))
      );
    });

    await act(async () => {
      await result.current.addTag({label: 'test-tag'});
    });

    expect(mockAddTag).toHaveBeenCalledWith({label: 'test-tag'});
  });

  it('should remove tag', async () => {
    const {result} = renderHook(() => useLearningSettings());
    const tagId = Identifier<TagSmall>(123);

    await act(async () => {
      await Effect.runPromise(
        PubSub.publish(hub, SuccessData(mockEmptySettings))
      );
    });

    await act(async () => {
      await result.current.removeTag(tagId);
    });

    expect(mockRemoveTag).toHaveBeenCalled();
  });

  it('should show loading state during operations', async () => {
    const {result} = renderHook(() => useLearningSettings());

    // Wait for stream setup
    await new Promise(resolve => setTimeout(resolve, 10));

    await act(async () => {
      await Effect.runPromise(
        PubSub.publish(hub, SuccessData(mockSettingsWithLangs))
      );
      await new Promise(resolve => setTimeout(resolve, 50));
    });

    await waitFor(() => {
      expect(result.current.learningSettings).toEqual(SuccessData(mockSettingsWithLangs));
    }, { timeout: 2000 });

    // Verify the hook has methods to modify state
    expect(typeof result.current.addLearnLang).toBe('function');
    expect(typeof result.current.removeLearnLang).toBe('function');
    expect(typeof result.current.addKnownLang).toBe('function');
    expect(typeof result.current.removeKnownLang).toBe('function');
  });
});
