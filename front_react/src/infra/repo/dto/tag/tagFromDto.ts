import {Schema} from 'effect';
import type {Tag} from '../../../../domain/Tag.ts';
import type {TagDTO} from './tagDto.ts';
import {identifierFromNumber} from '../transformers/identifierFromNumber.ts';
import type {Student} from '../../../../domain/Student.ts';

export const tagFromDto: Schema.Schema<Tag, TagDTO> =
    Schema.Struct({
      label: Schema.String,
      ownerId: identifierFromNumber<Student>()
    })
