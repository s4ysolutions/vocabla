import {Schema} from 'effect';
import {schemaDefinitionDto} from './definitionDto.ts';
import {schemaDefinition} from '../../../../domain/Entry.ts';
import {schemaLocalized} from '../../../../domain/Localized.ts';

export const definitionFromDto = Schema.transform(
  schemaDefinition,
  schemaDefinitionDto,
  {
    encode: dto => schemaDefinition.make(schemaLocalized.make({s: dto.text, langCode: dto.langCode}), {})
    decode: domain => domain
  }
)
