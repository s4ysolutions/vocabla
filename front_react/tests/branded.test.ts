import {describe, expect, it} from '@effect/vitest';
import {Brand, Schema} from 'effect';
import {BrandTypeId} from 'effect/Brand';

describe('Branded types', () => {
  it('brand with same structure are not compatible', () => {
    type User = { name: string } & Brand.Brand<'User'>
    type Tag = { name: string } & Brand.Brand<'Tag'>
    const user: User = Brand.nominal<User>()({name: 'user1'})
    const tag: Tag = Brand.nominal<Tag>()({name: 'tag1'})
    expect(user).toEqual({name: 'user1'})
    expect(tag).toEqual({name: 'tag1'})
    /*
    const u: User = tag
    const t: Tag = user
    void u;
    void t;
     */
  })
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
    try {
      int(2.5)
      expect(true).toBe(false)
    } catch (e) {
      expect(e.length).toBe(1)
      expect((e[0] as Error).message).toBe('Expected 2.5 to be an integer')
    }
  })
  it('owned', () => {
    type ID = number
    type ProductId = ID & Brand.Brand<'ProductId'>
    const productId = Brand.nominal<ProductId>()
    type UserId = ID & Brand.Brand<'UserId'>
    const userId = Brand.nominal<UserId>()

    type Owned<T, ID extends ProductId | UserId> = T & { ownerId: ID }

    type Owned1 = Owned<{ payload1: string }, UserId>
    type Owned2 = Owned<{ payload2: string }, ProductId>

    const owned1: Owned1 = {payload1: 'data1', ownerId: userId(1)}
    const owned2: Owned2 = {payload2: 'data2', ownerId: productId(2)}

    expect(owned1.ownerId).toBe(1)
    expect(owned2.ownerId).toBe(2)
  })
  it('schema', () => {
    type ID = number
    const schemaUserId = Schema.Number.pipe(Schema.brand('UserId'))
    type UserId = Schema.Schema.Type<typeof schemaUserId>
    const schemaIdentifiedUser = Schema.Struct({
      id: schemaUserId,
      nickname: Schema.String
    }) satisfies Schema.Schema<{ id: UserId, nickname: string }, { id: ID, nickname: string }>
    const userId = Brand.nominal<UserId>()
    type IdentifiedUser = Schema.Schema.Type<typeof schemaIdentifiedUser>
    const user: IdentifiedUser = schemaIdentifiedUser.make({id: userId(42), nickname: 'Nick'})//{id: 1 as ID & Brand.Brand<'UserId'>, nickname: 'nick'}
    void (user satisfies IdentifiedUser)

    expect(user).toEqual({id: 42, nickname: 'Nick'})
  })
  it('extract', () => {
    type StudentBrandType = 'Student'
    type Student = {
      readonly nickname: string
    } & Brand.Brand<StudentBrandType>

    type ExtractBrandType<T> = T extends Brand.Brand<infer B> ? B : never
    type StudentBrand = ExtractBrandType<Student>

    const sb: StudentBrand = 'Student'
    expect(sb).toBe('Student')
  })
  it('use extract', () => {
    type Id<E extends string | symbol> = number & Brand.Brand<E>

    type StudentBrandType = 'Student'
    type Student = {
      readonly nickname: string
    } & Brand.Brand<StudentBrandType>

    //   type ExtractBrandType<T> = T extends Brand.Brand<infer B> ? B : never

//    type StudentBrand = ExtractBrandType<Student>

    type Identifier<E> = E extends Brand.Brand<infer B>
      ? Id<B>
      : never

    type StudentId = Identifier<Student> // number & Brand.Brand<'Student'>

    type Tag = {
      label: string
      ownerId: Identifier<Student>
    }

    const tag: Tag = {label: 'tag1', ownerId: 1 as StudentId}
    expect(tag.ownerId).toBe(1)
  })
})
