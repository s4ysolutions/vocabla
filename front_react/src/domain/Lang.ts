import {Schema} from 'effect';
import {LangCode, schemaLangCode} from './LangCode.ts';

export const schemaLang = Schema.Struct({
  code: schemaLangCode,
  name: Schema.String,
  flag: Schema.optional(Schema.String)
})

export type Lang = Schema.Schema.Type<typeof schemaLang>

export const Lang = (code: string, name: string, flag?: string): Lang =>
  schemaLang.make({code: LangCode(code), name, flag})
