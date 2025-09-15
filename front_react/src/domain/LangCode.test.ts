import {describe, expect, it} from '@effect/vitest';
import {Option, pipe, Schema, SchemaAST} from 'effect';
import {isLangCode, LangCode, schemaLangCode} from './LangCode.ts';

describe('LangCode', () => {
  describe('constructors, assigns and annotation', () => {
    it('construct instance', () => {
      const langCode: LangCode = LangCode('en')
      expect(langCode).toBe('en');
    });
    it('instance assigns', () => {
      const langCode: LangCode = LangCode('en')
      //expect(typeof langCode).toBe("LangCode");❌
      const lc: string = langCode; // ✅
      void lc;
      expect(typeof langCode).toBe('string');
    });
    it('schema name', () => {
      expect(schemaLangCode.name).toBe('BrandClass');
    });
    it.skip('schame annotaion', () => {
      const schema = schemaLangCode;
      expect(SchemaAST.getSchemaIdAnnotation(schema.ast)).toEqual(Option.some('LangCode'));
      expect(pipe(
        SchemaAST.getAnnotation(SchemaAST.SchemaIdAnnotationId)(schema.ast),
        Option.getOrNull))
        .toBe('LangCode');
    });
    it('type guard', () => {
      const langCode: LangCode = LangCode('en')
      expect(isLangCode(langCode)).toBe(true);
    });
    it('transform from string', () => {
      const schemaBranded = Schema.Struct({value: Schema.String})

      const fromString = Schema.String.pipe(
        Schema.transform(schemaBranded, {
            strict: true,
            encode: (input) => input.value,         // { value: string } -> string
            decode: (str) => ({value: str})       // string -> { value: string }
          }
        )
      )
      type B = typeof schemaBranded.Type
      const b: B = Schema.decodeSync(fromString)('123')
      const bConstruct = (s: string) => schemaBranded.make({value: s})
      const b2: B = bConstruct('123')
      expect(b).toEqual(b2);
      expect(b).toEqual({value: '123'});
    });
  });
  describe('decoding (parsing from JSON)', () => {
    it('should decode valid language codes', () => {
      const validCodes = ['en', 'fr', 'de', 'es', 'ja', 'zh']
      validCodes.forEach(code => {
        const result = Schema.decodeUnknownSync(schemaLangCode)(code)
        expect(result).toBe(code)
        expect(isLangCode(result)).toBe(true)
      })
    })
    it('should decode valid language codes with country', () => {
      const validCodes = ['en-US', 'en-GB', 'fr-FR', 'de-DE', 'zh-CN']

      validCodes.forEach(code => {
        const result = Schema.decodeUnknownSync(schemaLangCode)(code)
        expect(result).toBe(code)
        expect(isLangCode(result)).toBe(true)
      })
    })
    it('should fail to decode invalid formats', () => {
      const invalidCodes = [
        'english',       // too long
        'EN',           // uppercase language
        'en-us',        // lowercase country
        'en-USA',       // country too long
        'e',            // too short
        'en-U',         // country too short
        'en_US',        // underscore instead of hyphen
        '123',          // numbers
        '',             // empty string
        'en-',          // incomplete
        '-US',          // incomplete
      ]

      invalidCodes.forEach(code => {
        expect(() => Schema.decodeUnknownSync(schemaLangCode)(code))
          .toThrow(/Invalid language code format/)
      })
    })
    it('should fail to decode non-string values', () => {
      const nonStringValues = [123, null, undefined, {}, [], true]

      nonStringValues.forEach(value => {
        expect(() => Schema.decodeUnknownSync(schemaLangCode)(value))
          .toThrow()
      })
    })
    it('should handle JSON parsing from string', () => {
      const jsonString = '"en-US"'
      const parsed = JSON.parse(jsonString)
      const result = Schema.decodeUnknownSync(schemaLangCode)(parsed)

      expect(result).toBe('en-US')
      expect(isLangCode(result)).toBe(true)
    })
  });
  describe('encoding (serializing to JSON)', () => {
    it('should encode LangCode to string', () => {
      const langCode = LangCode('en-US')
      const encoded = Schema.encodeSync(schemaLangCode)(langCode)

      expect(encoded).toBe('en-US')
      expect(typeof encoded).toBe('string')
    })
    it('should encode to JSON string', () => {
      const langCode = LangCode('fr-FR')
      const encoded = Schema.encodeSync(schemaLangCode)(langCode)
      const jsonString = JSON.stringify(encoded)

      expect(jsonString).toBe('"fr-FR"')
    })
    it('should roundtrip encode/decode correctly', () => {
      const originalCodes = ['en', 'fr-FR', 'de-DE', 'ja', 'zh-CN']

      originalCodes.forEach(code => {
        const langCode = LangCode(code)
        const encoded = Schema.encodeSync(schemaLangCode)(langCode)
        const decoded = Schema.decodeUnknownSync(schemaLangCode)(encoded)

        expect(decoded).toBe(code)
        expect(isLangCode(decoded)).toBe(true)
      })
    })
  });
});
