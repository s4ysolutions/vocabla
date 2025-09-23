import {describe, it, expect} from '@effect/vitest';
import { Schema } from 'effect';
import { schemaLangFromDto } from './langFromDto';
import { type LangDto } from './langDto';
import { type Lang } from '../../../../domain/Lang.ts';
import {LangCode} from '../../../../domain/LangCode.ts';

describe('schemaLangFromDto', () => {
  const langDto: LangDto = {
    code: 'en',
    name: 'English',
    flag: '🇬🇧',
  };

  const lang: Lang = {
    code: LangCode('en'),
    name: 'English',
    flag: '🇬🇧',
  };

  it('decodes LangDto to Lang', () => {
    const result = Schema.decodeSync(schemaLangFromDto)(langDto);
    expect(result).toEqual(lang);
  });

  it('encodes Lang to LangDto', () => {
    const result = Schema.encodeSync(schemaLangFromDto)(lang);
    expect(result).toEqual(langDto);
  });

  it.skip('handles missing optional flag (decode)', () => {
    const dto: LangDto = { code: 'pl', name: 'Polski' } as unknown as LangDto;
    const result = Schema.decodeSync(schemaLangFromDto)(dto);
    expect(result).toEqual({ code: 'pl', name: 'Polski', flag: undefined });
  });

  it('handles missing optional flag (encode)', () => {
    const l: Lang = { code: 'pl', name: 'Polski' } as unknown as Lang;
    const result = Schema.encodeSync(schemaLangFromDto)(l);
    expect(result).toEqual({ code: 'pl', name: 'Polski', flag: '❓' });
  });

  it('throws on invalid code type', () => {
    const badDto = { code: 123, name: 'Invalid', flag: '❓' } as unknown as LangDto;
    expect(() => Schema.decodeSync(schemaLangFromDto)(badDto)).toThrow();
  });
});

