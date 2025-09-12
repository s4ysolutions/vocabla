import {describe, expect, it} from '@effect/vitest';
import {type Identifier} from './Identifier.ts';


describe('Identifier', () => {
  it('should exist', () => {
    type User = { name: string }
    type Tag = { label: string }

    const userId: Identifier<User> = 1234
    const userI2: Identifier<User> = userId
    const tagId: Identifier<Tag> = 1234

    expect(userId).toBe(1234)
    expect(tagId).toBe(1234)
    expect(userId).toBe(userI2) //   I do not care about runtime equality
    expect(userId).toBe(tagId) //  I do not care about runtime equality
    expect(userId).toEqual(tagId) //  I do not care about runtime equality

    type T = Identifier<User> | Identifier<Tag>
    const id: T = userId
    expect(id).toBe(userId)
    const id2: T = tagId
    expect(id2).toBe(tagId)

    //const wrongAssignment: Identifier<Tag> = userId;//❌ must not compile
    //void wrongAssignment;
  });
});
