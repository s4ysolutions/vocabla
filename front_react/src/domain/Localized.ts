import {Schema} from 'effect';
import {schemaLangCode} from './LangCode.ts';

export const schemaLocalized = Schema.Struct({
  langCode: schemaLangCode,
  s: Schema.String
})
export type Localized = typeof schemaLocalized.Type
export const localized = (langCode: string, s: string): Localized =>
  Schema.decodeSync(schemaLocalized)({langCode, s})
