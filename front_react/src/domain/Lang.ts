import {Schema} from 'effect';
import {langCode, schemaLangCode} from './LangCode.ts';

const _schemaLang = Schema.Struct({
  code: schemaLangCode,
  name: Schema.String,
  flag: Schema.optional(Schema.String)
})

export const schemaLang = Schema.asSchema(_schemaLang)

export type Lang = Schema.Schema.Type<typeof schemaLang>
export const lang = (code: string, name: string, flag?: string): Lang =>
  _schemaLang.make({code: langCode(code), name, flag})
