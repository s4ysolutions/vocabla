import {Schema} from 'effect';

export const typeFromProp =
  <T, DTO>(propName: string, transformer: Schema.Schema<T, DTO>): Schema.Schema<T, { [propName]: DTO }> => {
    const extractProp = Schema.transform(
      Schema.Struct({[propName]: Schema.Any}),
      Schema.Any,
      {
        decode: (obj) => obj[propName],
        encode: (value) => ({[propName]: value})
      }
    )

    return Schema.compose(extractProp, transformer)
  }
