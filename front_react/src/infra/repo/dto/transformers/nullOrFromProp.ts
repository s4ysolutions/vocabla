import {Schema, Option} from 'effect';

export const nullOrFromProp =
  <T, DTO>(propName: string, transformer: Schema.Schema<T, DTO>): Schema.Schema<Option.Option<T>, unknown> => {
    return Schema.transform(
      Schema.Unknown,
      Schema.OptionFromSelf(transformer),
      {
        decode: (obj) => {
          const r = obj as Record<string, unknown>
          const value = r[propName]

          if (value === null) {
            return Option.none()
          }

          return Option.some(value as DTO) // Не декодируем здесь!
        },
        encode: (maybeValue) => {
          if (Option.isNone(maybeValue)) {
            return {}
          }
          return { [propName]: maybeValue.value }
        }
      }
    )
  }
