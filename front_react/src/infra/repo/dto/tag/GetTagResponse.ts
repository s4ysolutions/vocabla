import type {components} from '../../../rest/types.ts';
import {Schema, Option} from 'effect';
import {tagFromDto} from './tagFromDto.ts';
import {schemaTag, type Tag} from '../../../../domain/Tag.ts';

export type GetTagResponse = components['schemas']['GetTagResponse']
const schemaGetTagResponse: Schema.Schema<Option.Option<Tag>, GetTagResponse> =
  Schema.Struct({
    tag: Schema.OptionFromNullOr(tagFromDto)
  }).pipe(
    Schema.transform(
      Schema.OptionFromSelf(schemaTag),
      {
        decode: (response) => response.tag,
        encode: (maybeTag) => ({ tag: maybeTag })
      }
    )
  )as Schema.Schema<Option.Option<Tag>, GetTagResponse> // TODO: fix this cast

export const decodeGetTagResponse = Schema.decode(schemaGetTagResponse)
