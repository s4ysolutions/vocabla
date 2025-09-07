import {Schema, Option} from 'effect';

/**
 * Schema transformer that extracts an optional property from an object and applies a given transformer to it.
 * If the property is absent, it results in `Option.none()`. If present, it applies the transformer and wraps the result in `Option.some()`.
 *
 * @example
 * ```ts
 * import {Schema} from 'effect';
 * import {optionalFromProp} from './optionalFromProp.ts';
 * @param propName
 * @param transformer
 */
export const optionalFromProp =
  <T, DTO>(propName: string, transformer: Schema.Schema<T, DTO>): Schema.Schema<Option.Option<T>, unknown> => {
    return Schema.transform(
      Schema.Unknown,
      Schema.OptionFromSelf(transformer),
      {
        decode: (obj) => {
          const r = obj as Record<string, unknown>
          const value = r[propName]

          if (value === null || value === undefined) {
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
