import {ParseResult, Schema} from 'effect';
import type {components} from '../../rest/types.ts';
import {schemaTag, type Tag} from '../../../domain/Tag.ts';
import {ownedFromNumber} from './ownedFromNumber.ts';
import type {Student} from '../../../domain/Student.ts';

type TagDTO = components['schemas']['Tag']
const schemaOwnedByStudent = ownedFromNumber<Student>()

export const tagFromResponse: Schema.Schema<Tag, TagDTO> = Schema.transformOrFail(
  Schema.Struct({ //dto
    label: Schema.String,
    ownerId: Schema.Number,
  }),
  schemaTag, // domain
  {
    decode: (dto, _, ast) =>
      Schema.decode(schemaOwnedByStudent)(dto).pipe(
        ParseResult.mapError((parseError) =>
          new ParseResult.Type(ast, dto, parseError.message)
        ),
        ParseResult.map((owned) => ({
          label: dto.label,
          ...owned
        })),
      ),
    encode: (domain, _, ast) =>
      ParseResult.fail(new ParseResult.Forbidden(ast, domain, 'Encoding is not supported')),
    strict: true
  }
)
