import {Brand, Schema} from 'effect'


export type LangCode = string & Brand.Brand<'LangCode'>

//const LANG_CODE_REGEX = /^[a-z]{2}[a-z]?(-[A-Z]{2}[A-Z]?)?$/
const LANG_CODE_REGEX = /^[a-z]{2}[a-z]?$/
const isValidLangCode = (lc: string): boolean =>
  LANG_CODE_REGEX.test(lc) || lc === 'map-bms'
/*
const schemaLangCodeStrict: Schema.Schema<LangCode, string> = Schema.String.pipe(
  Schema.filter((input) =>{
    const b = isValidLangCode(input)
    if (!b)
      console.trace(`Invalid language code format: ${input}`)
    return b
  }, {
    message: (s) => // TODO: s is ParseIssue should not be treated as string
      'Invalid language code format: ' + s
  }),
  Schema.brand('LangCode'),
)
*/

export const schemaLangCode: Schema.Schema<LangCode, string> = Schema.String.pipe(
  Schema.transform(
    Schema.String.pipe(Schema.brand('LangCode')), // Apply brand to a base schema for the target
    {
      decode: (input) => {
        if (isValidLangCode(input)) {
          return input as LangCode;
        } else {
          console.trace(`Invalid language code format: ${input}`); // Logging preserved
          return '' as LangCode; // Default value on invalid input
        }
      },
      encode: (branded) => branded,
    }
  ),
);

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

export const emptyLangCode: LangCode = LangCode('')
export const isEmptyLangCode = (lc: LangCode) => lc === emptyLangCode

//export const schemaLangCode: Schema.Schema<LangCode> = Schema.declare(
/*
export const schemaLangCode = Schema.declare(
  (input): input is LangCode =>
    typeof input === 'string' && isValidLangCode(input),
  {
    decode: (input: unknown) => {
      if (typeof input !== 'string') {
        return ParseResult.fail(new ParseResult.Type(Schema.String.ast, input, 'Input must be a string'))
      }

      if (!isValidLangCode(input)) {
        return ParseResult.fail(new ParseResult.Type(Schema.String.ast, input, 'Invalid language code format'))
      }

      return ParseResult.succeed(input as LangCode)
    },

    encode: (langCode: unknown) => ParseResult.succeed(langCode),
  }
)
 */
