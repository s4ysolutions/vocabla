import {Schema} from 'effect';
import {type DefinitionDTO, schemaDefinitionDto} from './definitionDto.ts';
import {Definition, schemaDefinition} from '../../../../domain/Entry.ts';
import {Localized} from '../../../../domain/Localized.ts';
import {LangCode} from '../../../../domain/LangCode.ts';

export const definitionFromDto: Schema.Schema<Definition, DefinitionDTO> = Schema.transform(
  schemaDefinitionDto,
  schemaDefinition,
  {
    decode: (dto) => {
      return Definition(Localized(LangCode(dto.langCode), dto.definition))
    },
    encode: domain =>
      ({langCode: domain.localized.langCode, definition: domain.localized.s}),
    strict: true
  }
)
