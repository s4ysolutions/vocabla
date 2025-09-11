import {describe, expect, it} from '@effect/vitest';
import {Brand} from 'effect';
import {BrandTypeId} from 'effect/Brand';

describe('Branded types', () => {
  it('nominal', () => {
    type ID = number
    type ProductId = ID & Brand.Brand<'ProductId'>
    type UserId = ID & Brand.Brand<'UserId'>

    const userId: UserId = Brand.nominal<UserId>()(1)
    console.log(userId)
    expect(userId).toBe(1)
    const productId: ProductId = Brand.nominal<ProductId>()(2)
    expect(productId[BrandTypeId]).toBeUndefined()
  })
  it('refined', () => {
    type ID = number
    type Int = ID & Brand.Brand<'Int'>
    const int = Brand.refined<Int>(
      (n) => Number.isInteger(n),
      (n) =>
        Brand.error(`Expected ${n} to be an integer`)
    )
    const id2 = int(2)
    expect(id2).toBe(2)
    try{
      int(2.5)
      expect(true).toBe(false)
    } catch(e){
      expect(e.length).toBe(1)
      expect((e[0] as Error).message).toBe('Expected 2.5 to be an integer')
    }
  })
})
