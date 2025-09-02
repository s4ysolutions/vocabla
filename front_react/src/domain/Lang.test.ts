import {describe, expect, it} from '@effect/vitest';
import {lang, type Lang} from './Lang.ts';

describe('Lang', () => {
  it('type should exist', () => {
    const l: Lang = lang('en', 'English', '🇬🇧');
    expect(l).toEqual({code: 'en', name: 'English', flag: '🇬🇧'});
  });
});
