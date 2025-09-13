import type {components} from '../../../rest/types.ts';
import {Schema} from 'effect';
import type {Tag} from '../../../../domain/Tag.ts';
import {Identifier, schemaIdentifier} from '../../../../domain/identity/Identifier.ts';

export type CreateTagResponse = components['schemas']['CreateTagResponse'];

const schemaCreateTagResponseDto =
  Schema.Struct({
    tagId: Schema.Number
  })

const check1: CreateTagResponse = {} as Schema.Schema.Type<typeof schemaCreateTagResponseDto>;
void check1
const check2: Schema.Schema.Type<typeof schemaCreateTagResponseDto> = {} as CreateTagResponse;
void check2
void (schemaCreateTagResponseDto satisfies Schema.Schema<CreateTagResponse>)

const schemaCreateTagResponse: Schema.Schema<Identifier<Tag>, CreateTagResponse> =
  Schema.transform(
    schemaCreateTagResponseDto,
    schemaIdentifier<Tag>(),
    {
      decode: ({tagId}) => Identifier<Tag>(tagId),
      encode: (id) => ({tagId: id}),
      strict: true,
    })

void (schemaCreateTagResponse satisfies Schema.Schema<Identifier<Tag>, CreateTagResponse>)

export const decodeCreateTagResponse = Schema.decode(schemaCreateTagResponse)
