import {Schema} from 'effect';
import type {components} from '../../../rest/types.ts';
import type {Student} from '../../../../domain/Student.ts';
import {identifierFromNumber} from '../identifierFromNumber.ts';
import type {Tag} from '../../../../domain/Tag.ts';

type TagDTO = components['schemas']['Tag']
const identifierFromNumberStudent = identifierFromNumber<Student>()

export const tagFromDto: Schema.Schema<Tag, TagDTO> =
  Schema.Struct({ //dto
    label: Schema.String,
    ownerId: identifierFromNumberStudent
  })
