import {describe, it, expect} from '@effect/vitest';
describe('infer', () => {
  it('get array elem type', () => {
    type ElementType<T> = T extends (infer U)[] ? U : T;

    type A = ElementType<string[]>
    type B = ElementType<number[]>
    type C = ElementType<boolean>

    const a: A = 'hello'
    const b: B = 42
    const c: C = true

    expect(a).toBeTypeOf('string')
    expect(b).toBeTypeOf('number')
    expect(c).toBeTypeOf('boolean')
  });
})
