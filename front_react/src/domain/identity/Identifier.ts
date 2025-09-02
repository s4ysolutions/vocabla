import {Schema} from 'effect';

export const schemaIdentifier = <T>() =>
  Schema.Struct({
    value: Schema.Number,
    //__phantom: Schema.optional(Schema.declare<T>((_): _ is T => true))  // phantom поле
    __phantom: Schema.optional(Schema.Unknown as Schema.Schema<T>) // phantom поле
  })

export type Identifier<E> = Schema.Schema.Type<ReturnType<typeof schemaIdentifier<E>>>
export const id = <E>(value: number): Identifier<E> =>
  Schema.decodeSync(schemaIdentifier<E>())({value})
