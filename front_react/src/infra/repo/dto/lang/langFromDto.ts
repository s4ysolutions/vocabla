import {Schema} from 'effect';
import {type LangDto, schemaLangDto} from './langDto.ts';
import {type Lang, schemaLang} from '../../../../domain/Lang.ts';
import {schemaLangCodeFromDto} from './langCodeFromDto.ts';
import {LangCode} from '../../../../domain/LangCode.ts';

export const schemaLangFromDto: Schema.Schema<Lang, LangDto> =
  Schema.transform(
    schemaLangDto,
    Schema.Struct({
      code: schemaLangCodeFromDto,
      name: Schema.String,
      flag: Schema.optional(Schema.String)
    }),
    {
      decode: (dto) => ({
        code: dto.code,
        name: dto.name,
        flag: dto.flag,
      }),
      encode: (domain) => ({
        code: domain.code,
        name: domain.name,
        flag: domain.flag || '❓',
      }),
      strict: true
    }
  ).pipe(
    Schema.transform(
      schemaLang,
      {
        decode: (dto) => schemaLang.make({
          code: dto.code,
          name: dto.name,
          flag: dto.flag,
        }),
        encode: (domain) => ({
          code: LangCode(domain.code),
          name: domain.name,
          flag: domain.flag || '❓',
        }),
        strict: true
      }
    )
  )

void (schemaLangFromDto satisfies Schema.Schema<Lang, LangDto>)

const check1: Lang = {} as Schema.Schema.Type<typeof schemaLangFromDto>;
void check1
const check2: Schema.Schema.Type<typeof schemaLangFromDto> = {} as Lang;
void check2

void ({} as Lang satisfies Schema.Schema.Type<typeof schemaLangFromDto>)
void ({} as Schema.Schema.Type<typeof schemaLangFromDto> satisfies Lang)
