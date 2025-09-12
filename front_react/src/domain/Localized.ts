import {Schema} from 'effect';
import {type LangCode, schemaLangCode} from './LangCode.ts';

export const schemaLocalized = Schema.Struct({
  langCode: schemaLangCode,
  s: Schema.String
})
export type Localized = typeof schemaLocalized.Type
export const Localized = (langCode: LangCode, s: string): Localized =>
  ({langCode, s})
