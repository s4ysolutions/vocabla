import type {DeepReadonly} from '../DeepReadonly.ts';
import type {components} from '../../../rest/types.ts';
import {Effect, Schema} from 'effect';
import {schemaLangDto} from './langDto.ts';
import {schemaLangFromDto} from './langFromDto.ts';
import type {Lang} from '../../../../domain/Lang.ts';
import type {ParseError} from 'effect/ParseResult';

export type GetLanguagesResponseDto = DeepReadonly<components['schemas']['GetLanguagesResponse']>;
export const schemaGetLanguagesResponseDto = Schema.Struct({
  defaultLang: schemaLangDto,
  unknownLang: schemaLangDto,
  languages: Schema.Array(schemaLangDto)
});

const _check1: GetLanguagesResponseDto = {} as Schema.Schema.Type<typeof schemaGetLanguagesResponseDto>;
void _check1;
const _check2: Schema.Schema.Type<typeof schemaGetLanguagesResponseDto> = {} as GetLanguagesResponseDto;
void _check2;

void ({} as GetLanguagesResponseDto satisfies Schema.Schema.Type<typeof schemaGetLanguagesResponseDto>);
void ({} as Schema.Schema.Type<typeof schemaGetLanguagesResponseDto> satisfies GetLanguagesResponseDto);
void (schemaGetLanguagesResponseDto satisfies Schema.Schema<GetLanguagesResponseDto>);

export const schemaGetLanguagesResponse = Schema.Struct({
  defaultLang: schemaLangFromDto,
  unknownLang: schemaLangFromDto,
  languages: Schema.Array(schemaLangFromDto)
});

const _check3: {
  defaultLang: Lang,
  unknownLang: Lang,
  languages: ReadonlyArray<Lang>
} = {} as Schema.Schema.Type<typeof schemaGetLanguagesResponse>;
void _check3
const _check4: Schema.Schema.Type<typeof schemaGetLanguagesResponse> = {} as {
  defaultLang: Lang,
  unknownLang: Lang,
  languages: Array<Lang>
}
void _check4
const _check5: GetLanguagesResponseDto = {} as Schema.Schema.Encoded<typeof schemaGetLanguagesResponse>
void _check5
const _check6: Schema.Schema.Encoded<typeof schemaGetLanguagesResponse> = {} as GetLanguagesResponseDto
void _check6

void (schemaGetLanguagesResponse satisfies Schema.Schema<{
  readonly defaultLang: Lang,
  readonly unknownLang: Lang,
  readonly languages: ReadonlyArray<Lang>
}, GetLanguagesResponseDto>)

export const decodeGetLanguagesResponse = Schema.decode(schemaGetLanguagesResponse)

type Decoder = (input: GetLanguagesResponseDto) => Effect.Effect<{
  readonly defaultLang: Lang,
  readonly unknownLang: Lang,
  readonly languages: ReadonlyArray<Lang>
}, ParseError>;
const _checkDecoder: Decoder = decodeGetLanguagesResponse;
void _checkDecoder
