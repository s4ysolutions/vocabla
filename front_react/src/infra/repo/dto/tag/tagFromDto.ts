import {Schema} from 'effect';
import type {Tag} from '../../../../domain/Tag.ts';
import {identifierFromNumberStudent} from '../student/identifier.ts';
import type {TagDTO} from './tagDto.ts';

export const tagFromDto: Schema.Schema<Tag, TagDTO> =
  Schema.Struct({ //dto
    label: Schema.String,
    ownerId: identifierFromNumberStudent
  })
