import {Schema} from 'effect';
import {schemaTagSmall, type TagSmall} from '../../../../../domain/TagSmall.ts';
import schemaTagSmallDto, {type TagSmallDto} from './schemaTagSmallDto.ts';

const schemaTagSmallFromDto: Schema.Schema<TagSmall, TagSmallDto> = Schema.transform(
  schemaTagSmallDto,
  schemaTagSmall,
  {
    decode: (tagSmallDto) => tagSmallDto,
    encode: (tagSmall) => tagSmall,
    strict: true
  }
)

export default schemaTagSmallFromDto
