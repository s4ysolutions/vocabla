import {describe, expect, it} from '@effect/vitest';
import {Lang} from './Lang.ts';

describe('Lang', () => {
  it('type should exist', () => {
    const l: Lang = Lang('en', 'English', '🇬🇧');
    expect(l).toEqual({code: 'en', name: 'English', flag: '🇬🇧'});
  });
});
