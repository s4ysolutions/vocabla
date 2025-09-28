import {type Identifier} from './identity/Identifier.ts';
import {Schema} from 'effect';

export type TagSmall = {
  label: string,
}

export const Tag = (label: string): TagSmall => ({label})

export type TagSmallId = Identifier<TagSmall>

export const schemaTagSmall = Schema.Struct({
  label: Schema.String,
})
