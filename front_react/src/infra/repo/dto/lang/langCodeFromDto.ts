import {Schema} from 'effect';
import {schemaLangCodeDto} from './langCodeDto.ts';
import {schemaLangCode} from '../../../../domain/LangCode.ts';

export const langCodeFromDto = Schema.transform(
  schemaLangCode,
  schemaLangCodeDto,
  {
    encode: dto => schemaLangCode.make(dto),
    decode: domain => domain
  }
)
