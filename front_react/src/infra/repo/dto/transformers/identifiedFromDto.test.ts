import {describe, it, expect} from '@effect/vitest';
import {Schema} from 'effect';
import {identifiedFromDto} from './identifiedFromDto.ts';
import {schemaIdentifier, Identifier} from '../../../../domain/identity/Identifier.ts';
import type {Identified} from '../../../../domain/identity/Identified.ts';

// Test entity types
type TestEntity = {
  name: string;
  value: number;
};

type TestEntityDto = {
  name: string;
  value: number;
};

// Create test schemas
const testEntitySchema: Schema.Schema<TestEntity, TestEntityDto> = Schema.Struct({
  name: Schema.String,
  value: Schema.Number
});

const testIdentifierSchema = schemaIdentifier<TestEntity>();
const identifiedTestEntitySchema = identifiedFromDto(testIdentifierSchema, testEntitySchema);

describe('identifiedFromDto', () => {
  describe('decode', () => {
    it('should decode valid identified DTO to domain object', () => {
      // arrange
      const input: {id: number, e: TestEntityDto} = {
        id: 42,
        e: {
          name: 'test entity',
          value: 123
        }
      };
      const expectedOutput: Identified<TestEntity> = {
        id: Identifier<TestEntity>(42),
        e: {
          name: 'test entity',
          value: 123
        }
      };

      // act
      const result = Schema.decodeUnknownSync(identifiedTestEntitySchema)(input);

      // assert
      expect(result).toEqual(expectedOutput);
    });

    it('should fail to decode when id is missing', () => {
      // arrange
      const input = {
        e: {
          name: 'test entity',
          value: 123
        }
      };

      // act & assert
      expect(() => Schema.decodeUnknownSync(identifiedTestEntitySchema)(input)).toThrow();
    });

    it('should fail to decode when e property is missing', () => {
      // arrange
      const input = {
        id: 42
      };

      // act & assert
      expect(() => Schema.decodeUnknownSync(identifiedTestEntitySchema)(input)).toThrow();
    });

    it('should fail to decode when id is not a number', () => {
      // arrange
      const input = {
        id: 'not-a-number',
        e: {
          name: 'test entity',
          value: 123
        }
      };

      // act & assert
      expect(() => Schema.decodeUnknownSync(identifiedTestEntitySchema)(input)).toThrow();
    });

    it('should fail to decode when entity properties are invalid', () => {
      // arrange
      const input = {
        id: 42,
        e: {
          name: 123, // should be string
          value: 'not-a-number' // should be number
        }
      };

      // act & assert
      expect(() => Schema.decodeUnknownSync(identifiedTestEntitySchema)(input)).toThrow();
    });
  });

  describe('encode', () => {
    it('should encode domain object to identified DTO', () => {
      // arrange
      const input = {
        id: Identifier<TestEntity>(42),
        e: {
          name: 'test entity',
          value: 123
        }
      };
      const expectedOutput = {
        id: 42,
        e: {
          name: 'test entity',
          value: 123
        }
      };

      // act
      const result = Schema.encodeSync(identifiedTestEntitySchema)(input);

      // assert
      expect(result).toEqual(expectedOutput);
    });

    it('should encode with different entity values', () => {
      // arrange
      const input = {
        id: Identifier<TestEntity>(999),
        e: {
          name: 'another entity',
          value: 456
        }
      };
      const expectedOutput = {
        id: 999,
        e: {
          name: 'another entity',
          value: 456
        }
      };

      // act
      const result = Schema.encodeSync(identifiedTestEntitySchema)(input);

      // assert
      expect(result).toEqual(expectedOutput);
    });
  });

  describe('round-trip', () => {
    it('should maintain data integrity through decode-encode cycle', () => {
      // arrange
      const originalDto = {
        id: 777,
        e: {
          name: 'round-trip test',
          value: 789
        }
      };

      // act
      const decoded = Schema.decodeUnknownSync(identifiedTestEntitySchema)(originalDto);
      const reencoded = Schema.encodeSync(identifiedTestEntitySchema)(decoded);

      // assert
      expect(reencoded).toEqual(originalDto);
    });
  });
});
