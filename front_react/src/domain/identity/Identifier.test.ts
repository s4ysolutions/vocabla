import {describe, expect, it} from '@effect/vitest';
import {type Identifier} from './Identifier.ts';


describe('Identifier', () => {
  it('should exist', () => {
    type User = { name: string }
    type Tag = { label: string }

    const userId: Identifier<User> = {value: 1234} // { value: 1234, __phantom: undefined }
    const userI2: Identifier<User> = userId
    const tagId: Identifier<Tag> = {value: 1234} //{ value: 1234, __phantom: undefined }

    expect(userId.value).toBe(1234)
    expect(tagId.value).toBe(1234)
    expect(userId).toBe(userI2)
    expect(userId).not.toBe(tagId) //
    expect(userId).toEqual(tagId) // ❌ I do not care about runtime equality

    type T = Identifier<User> | Identifier<Tag>
    const id: T = userId
    expect(id).toBe(userId)

    //const wrongAssignment: Identifier<Tag> = userId;//❌ must not compile
    //void wrongAssignment;
  });
});
