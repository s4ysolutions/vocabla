import {Schema} from 'effect';
import {type LangCodeDto, schemaLangCodeDto} from './langCodeDto.ts';
import {LangCode, schemaLangCode} from '../../../../domain/LangCode.ts';

export const schemaLangCodeFromDto: Schema.Schema<LangCode, LangCodeDto> = Schema.transform(
  schemaLangCodeDto,
  schemaLangCode,
  {
    decode: (dto) => LangCode(dto),
    encode: (domain) => domain as LangCodeDto,
    strict: true
  }
)

void (schemaLangCodeFromDto satisfies Schema.Schema<LangCode, LangCodeDto>)

const check1: LangCode = {} as Schema.Schema.Type<typeof schemaLangCodeFromDto>;
void check1
const check2: LangCodeDto = {} as Schema.Schema.Type<typeof schemaLangCodeFromDto>;
void check2

void ({} as LangCode satisfies Schema.Schema.Type<typeof schemaLangCodeFromDto>)
void ({} as Schema.Schema.Type<typeof schemaLangCodeFromDto> satisfies LangCode)
