import {Brand, Schema} from 'effect';

export type Id<E extends string|symbol> = number & Brand.Brand<E>

const schemaIdBase = Schema.Number

export const schemaId = <E extends string | symbol>(brand: E) =>
  schemaIdBase.pipe(Schema.brand(brand))

