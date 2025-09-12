import {Schema} from 'effect';

export const IdBrandTypeId: unique symbol = Symbol.for('vocable/ID')

export type Identifier<E> = number & { readonly [IdBrandTypeId]?: E }

export const schemaIdentifier = <E>(): Schema.Schema<Identifier<E>> =>
  Schema.Number as Schema.Schema<Identifier<E>>

export const Identifier  = <E>(value: number): Identifier<E> =>
  value as Identifier<E>
