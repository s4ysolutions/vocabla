import {describe, it, expect} from '@effect/vitest';
import {Schema} from 'effect';
import {identifierFromNumber} from './identifierFromNumber.ts';

type E = unknown;
const identifierFromNumberE = identifierFromNumber<E>()

describe('IdentifierFromNumber', () => {
  describe('decode', () => {
    it('42 should be decoded to {value: 42}', () => {
      // arrange
      const input: unknown = 42;
      const expectedOutput = 42;

      // act
      const result = Schema.decodeUnknownSync(identifierFromNumberE)(input);

      // assert
      expect(result).toEqual(expectedOutput);
    });
    it('"a" should fail) to decode', () => {
      // arrange
      const input: unknown = 'a';

      // act & assert
      expect(() => Schema.decodeUnknownSync(identifierFromNumberE)(input)).toThrow();
    })
  });
  describe('encode', () => {
    it('{value: 42} should be encoded to 42', () => {
      // arrange
      const input = 42;
      const expectedOutput = 42;

      // act
      const result = Schema.encodeSync(identifierFromNumberE)(input);

      // assert
      expect(result).toEqual(expectedOutput);
    });
  });
});
