import {describe, expect, it} from '@effect/vitest';
import {Schema} from 'effect';
import {schemaOwned} from '../../../../domain/mixins/Owned.ts';
import {ownedFromNumber} from './ownedFromNumber.ts';

describe('OwnedFromOwnerId', () => {
  describe('from {ownerId: {value: number}}', () => {
    it('constructed from {ownerId: {value: 42}}', () => {
      const dto = {ownerId: 42};
      const domain = Schema.decodeSync(schemaOwned())(dto)
      expect(domain).toEqual({ownerId: 42})
    })
  })
  describe('from {ownerId: number}', () => {
    it('constructed from {ownerId: 42}', () => {
      const dto = {ownerId: 42};
      const domain = Schema.decodeSync(ownedFromNumber())(dto)
      expect(domain).toEqual({ownerId: 42})
    })
  })
})
