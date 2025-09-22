import {Brand, ParseResult, Schema} from 'effect'


export type LangCode = string & Brand.Brand<'LangCode'>

const LANG_CODE_REGEX = /^[a-z]{2}[a-z]?(-[A-Z]{2}[A-Z]?)?$/
const isValidLangCode = (lc: string): boolean =>
  LANG_CODE_REGEX.test(lc) || lc === 'map-bms'
/*
export const schemaLangCode: Schema.Schema<LangCode, string> = Schema.String.pipe(
  Schema.filter(isValidLangCode, {
    message: () => 'Invalid language code format'
  }),
  Schema.brand('LangCode'),
)
*/

//export const schemaLangCode: Schema.Schema<LangCode> = Schema.declare(
export const schemaLangCode = Schema.declare(
  (input): input is LangCode =>
    typeof input === 'string' && isValidLangCode(input),
  {
    decode: (input: unknown) => {
      if (typeof input !== 'string') {
        return ParseResult.fail(new ParseResult.Type(Schema.String.ast, input))
      }

      if (!isValidLangCode(input)) {
        return ParseResult.fail(new ParseResult.Type(schemaLangCode.ast, input))
      }

      return ParseResult.succeed(input as LangCode)
    },

    encode: (langCode: unknown) => ParseResult.succeed(langCode)
  }
)

const _check1: LangCode = '' as Schema.Schema.Type<typeof schemaLangCode>
void _check1
const _check2: Schema.Schema.Type<typeof schemaLangCode> = '' as LangCode
void _check2
void ('' as LangCode satisfies Schema.Schema.Type<typeof schemaLangCode>)
void ('' as Schema.Schema.Type<typeof schemaLangCode> satisfies LangCode)

//export type LangCode = Schema.Schema.Type<typeof schemaLangCode>

export const LangCode =
  (langCode: string) => langCode as LangCode // TODO: validate?
export const isLangCode = Schema.is(schemaLangCode)
