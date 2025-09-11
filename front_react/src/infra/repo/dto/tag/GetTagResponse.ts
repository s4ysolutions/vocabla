import type {components} from '../../../rest/types.ts';
import {Schema, Option, Match} from 'effect';
import {schemaTag, type Tag} from '../../../../domain/Tag.ts';
import {identifier} from '../../../../domain/identity/Identifier.ts';
import type {Student} from '../../../../domain/Student.ts';

export type GetTagResponse = components['schemas']['GetTagResponse']

const schemaGetTagResponseDto = Schema.Struct({
  tag: Schema.optionalWith(Schema.NullOr(Schema.Struct({
    label: Schema.String,
    ownerId: Schema.Number,
  })), {exact: true})
})
// Ensure the schema matches the TypeScript type
void (schemaGetTagResponseDto satisfies Schema.Schema<GetTagResponse>)

const schemaGetTagResponse = //: Schema.Schema<Option.Option<Tag>, GetTagResponse> =
  Schema.transform(
    schemaGetTagResponseDto,
    Schema.Option(schemaTag),
    {
      decode: (response) => {
        return response.tag === undefined || response.tag === null
          ? Option.none()
          : Option.some({label: response.tag.label, ownerId: identifier<Student>(response.tag.ownerId)})
      },
      encode: (maybeTag) => Match.value(maybeTag).pipe(
        Match.tag('None', () => ({tag: null})),
        Match.tag('Some', (some) =>
          ({tag: {label: some.value.label, ownerId: some.value.ownerId.value}})),
        Match.exhaustive
      ),
      strict: true
    }
  )

void (schemaGetTagResponse satisfies Schema.Schema<Option.Option<Tag>, GetTagResponse>)
export const decodeGetTagResponse = Schema.decode(schemaGetTagResponse)
