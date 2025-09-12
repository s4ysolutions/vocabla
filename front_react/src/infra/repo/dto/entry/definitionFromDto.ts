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
      const defintion = Definition(Localized(LangCode(dto.langCode), dto.definition))
      return defintion
    },
    encode: domain =>
      schemaDefinitionDto.make({langCode: domain.localized.langCode, definition: domain.localized.s}),
    strict: true
  }
)
