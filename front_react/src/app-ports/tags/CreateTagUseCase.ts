import {Context} from 'effect';
import {type Tag} from '../../domain/Tag.ts';
import type {Effect} from 'effect/Effect';
import type {AppError} from '../errors/AppError.ts';
import type {Identifier} from '../../domain/identity/Identifier.ts';

export type CreateTagRequest = {
  readonly tag: Tag
}
/*
export const schemaCreateTagRequest = Schema.Struct({
  tag: schemaTag,
})
export type CreateTagRequest = typeof schemaCreateTagRequest.Type
 */

export type CreateTagResponse = {
  readonly id: Identifier<Tag>;
}
/*
export const schemaCreateTagResponse = schemaIdentifier<Tag>()
export type CreateTagResponse = typeof schemaCreateTagResponse.Type
*/

export interface CreateTagUseCase {
  createTag(request: CreateTagRequest): Effect<CreateTagResponse, AppError>
}

export class CreateTagUseCaseTag extends Context.Tag('CreateTagUseCaseTag')<
  CreateTagUseCaseTag,
  CreateTagUseCase
>() {
}
