import type {components} from '../../../rest/types.ts';
import {Schema, Option, Match} from 'effect';
import {schemaTag, type Tag} from '../../../../domain/Tag.ts';
import {tagFromDto} from './tagFromDto.ts';

export type GetTagResponse = components['schemas']['GetTagResponse']

const schemaGetTagResponseDto =
  Schema.Struct({
    tag: Schema.optionalWith(Schema.NullOr(Schema.Struct({
      label: Schema.String,
      ownerId: Schema.Number,
    })), {exact: true})
  })
void (schemaGetTagResponseDto satisfies Schema.Schema<GetTagResponse>)

const schemaGetTagResponse: Schema.Schema<Option.Option<Tag>, GetTagResponse> =
  Schema.transform(
    Schema.Struct({
      tag: Schema.optionalWith(Schema.NullOr(tagFromDto), {exact: true})
    }),
    Schema.Option(schemaTag),
    {
      decode: ({tag}) => tag === null || tag === undefined
        ? {_tag: 'None'} as const//Option.none()
        : {_tag: 'Some', value: tag} as const,//Option.some(tag),
      encode: (optTag) => ({
        tag: Match.value(optTag).pipe(
          Match.tag('None', () => null),
          Match.tag('Some', ({value}) => value),
          Match.exhaustive
        ),
      }),
      strict: true
    })

void (schemaGetTagResponse satisfies Schema.Schema<Option.Option<Tag>, GetTagResponse>)
export const decodeGetTagResponse = Schema.decode(schemaGetTagResponse)
