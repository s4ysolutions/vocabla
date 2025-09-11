import type {components} from '../../../rest/types.ts';
import {Schema} from 'effect';
import {typeFromProp} from '../transformers/typeFromProp.ts';
import {identifierFromNumber} from '../transformers/identifierFromNumber.ts';
import type {Tag} from '../../../../domain/Tag.ts';

export type CreateEntryResponse = components['schemas']['CreateTagResponse'];
export const decodeCreateTagResponse = Schema.decodeUnknown(
  typeFromProp('tagId',
    identifierFromNumber<Tag>()))
