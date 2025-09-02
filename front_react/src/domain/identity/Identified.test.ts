import {describe, expect, it} from '@effect/vitest';
import {type Identified} from './Identified.ts';

describe('Identified', () => {
  it('should exist', () => {
    type User = { name: string }

    const user: Identified<User> = {
      id: {value: 1234},
      e: {name: 'Alice'}
    }
    expect(user.id.value).toBe(1234)
  });
});
