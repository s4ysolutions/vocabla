import {Schema} from 'effect';
import {type LangCodeDto, schemaLangCodeDto} from './langCodeDto.ts';
import {LangCode, schemaLangCode} from '../../../../domain/LangCode.ts';

export const langCodeFromDto: Schema.Schema<LangCode, LangCodeDto> = Schema.transform(
  schemaLangCodeDto,
  schemaLangCode,
  {
    decode: (dto) => LangCode(dto),
    encode: (domain) => domain as LangCodeDto,
    strict: true
  }
)

void (langCodeFromDto satisfies Schema.Schema<LangCode, LangCodeDto>)

const check1: LangCode = {} as Schema.Schema.Type<typeof langCodeFromDto>;
void check1
const check2: LangCodeDto = {} as Schema.Schema.Type<typeof langCodeFromDto>;
void check2

void ({} as LangCode satisfies Schema.Schema.Type<typeof langCodeFromDto>)
void ({} as Schema.Schema.Type<typeof langCodeFromDto> satisfies LangCode)
