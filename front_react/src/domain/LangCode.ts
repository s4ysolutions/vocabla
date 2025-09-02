import {Schema} from 'effect'

const isValidLangCode = (lc: string): boolean =>
  /^[a-z]{2}(-[A-Z]{2})?$/.test(lc)


export const schemaLangCode = Schema.String.pipe(
  Schema.filter(isValidLangCode, {
    message: () => 'Invalid language code format'
  }),
  Schema.brand('LangCode'),
)

export type LangCode = typeof schemaLangCode.Type
export const langCode =
  (langCode: string) => schemaLangCode.make(langCode)
export const isLangCode = Schema.is(schemaLangCode)
//(value: unknown): value is LangCode =>
//typeof value === 'string' && Schema.is(LangCode)(value)
