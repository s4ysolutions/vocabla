import {describe, expect, it} from 'vitest';
import {renderHook, waitFor} from '@testing-library/react';
import useLearningSettings from './useLearningSettings.ts';
import {isSuccess, LoadingData} from '../../../../app-ports/types.ts';

describe('useLearningSettings', () => {
  it('should show loading state during operations', async () => {
    const {result} = renderHook(() => useLearningSettings());

    // Wait for stream setup
    await new Promise(resolve => setTimeout(resolve, 10));

    expect(result.current.learningSettings).toEqual(LoadingData());
    expect(typeof result.current.addLearnLang).toBe('function');
    expect(typeof result.current.removeLearnLang).toBe('function');
    expect(typeof result.current.addKnownLang).toBe('function');
    expect(typeof result.current.removeKnownLang).toBe('function');
  });

  it('should receive loaded settings', async () => {
    const {result} = renderHook(() => useLearningSettings());

    // Wait for stream setup
    await new Promise(resolve => setTimeout(resolve, 10));

    expect(result.current.learningSettings).toEqual(LoadingData());

    await waitFor(() => {
      expect(isSuccess(result.current.learningSettings)).toBe(true);
    }, {timeout: 2000});

  });
});
