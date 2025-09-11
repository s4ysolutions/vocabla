import {Schema} from 'effect';
import {langCodeFromDto} from '../lang/langCodeFromDto.ts';

export const definitionFromDto = Schema.Struct({
  definition: Schema.String,
  langCode: langCodeFromDto,
})
