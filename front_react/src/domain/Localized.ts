import {Schema} from 'effect';
import {type LangCode, schemaLangCode} from './LangCode.ts';

export type Localized = {langCode: LangCode, s: string}

export const schemaLocalized = Schema.Struct({
  langCode: schemaLangCode,
  s: Schema.String
})

const _check1: Localized = {} as Schema.Schema.Type<typeof schemaLocalized>;
void _check1
const _check2: Schema.Schema.Type<typeof schemaLocalized> = {} as Localized;
void _check2
void ({} as Localized satisfies Schema.Schema.Type<typeof schemaLocalized>)
void ({} as Schema.Schema.Type<typeof schemaLocalized> satisfies Localized)

export const Localized = (langCode: LangCode, s: string): Localized =>
  ({langCode, s})
