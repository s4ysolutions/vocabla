import {Schema} from 'effect';
import {type TagSmall} from '../../../../../domain/TagSmall.ts';
import schemaIdentifiedTagSmallDto, {type IdentifiedTagSmallDto} from './schemaIdentifiedTagSmallDto.ts';
import {type Identified, schemaIdentified} from '../../../../../domain/identity/Identified.ts';
import schemaTagSmallFromDto from './schemaTagSmallFromDto.ts';

const schemaIdentifiedTagSmallFromDto: Schema.Schema<Identified<TagSmall>, IdentifiedTagSmallDto> =
  schemaIdentified(schemaTagSmallFromDto);

void (schemaIdentifiedTagSmallDto satisfies Schema.Schema<IdentifiedTagSmallDto, IdentifiedTagSmallDto>)

export default schemaIdentifiedTagSmallFromDto
