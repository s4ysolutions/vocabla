import {Context, Schema} from 'effect';
import {schemaTag, type Tag} from '../domain/Tag.ts';
import {schemaIdentifier} from '../domain/identity/Identifier.ts';
import type {Student} from '../domain/Student.ts';
import type {Effect} from 'effect/Effect';
import type {AppError} from './errors/AppError.ts';

export const schemaCreateTagRequest = Schema.Struct({
  tag: schemaTag,
  ownerId: schemaIdentifier<Student>()
})
export type CreateTagRequest = typeof schemaCreateTagRequest.Type

export const schemaCreateTagResponse = schemaIdentifier<Tag>()
export type CreateTagResponse = typeof schemaCreateTagResponse.Type

export interface CreateTagUseCase {
  createTag(request: CreateTagRequest): Effect<CreateTagResponse, AppError>
}

export class CreateTagUseCaseTag extends Context.Tag('CreateTagUseCaseTag')<
  CreateTagUseCaseTag,
  CreateTagUseCase
>() {
}
