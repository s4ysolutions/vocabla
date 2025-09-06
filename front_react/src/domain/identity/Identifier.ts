import {Schema} from 'effect';

export const schemaIdentifier = <T>() =>
  Schema.Struct({
    value: Schema.Number,
    __phantom: Schema.optionalWith(Schema.Unknown as Schema.Schema<T>,{ exact: true }) // phantom поле
  })

export type Identifier<E> = Schema.Schema.Type<ReturnType<typeof schemaIdentifier<E>>>
export const id = <E>(value: number): Identifier<E> =>
  ({value} as Identifier<E>)
export const identifier = id
