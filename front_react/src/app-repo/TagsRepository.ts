import type {Identifier} from '../domain/identity/Identifier.ts';
import type {Tag} from '../domain/Tag.ts';
import type {Effect} from 'effect/Effect';
import {Context} from 'effect';
import type {InfraError} from './infraError.ts';

export interface TagsRepository {
  createTag: (tag: Tag)=> Effect<Identifier<Tag>, InfraError>
}

export class TagsRepositoryTag extends Context.Tag('TagsRepository')<
  TagsRepositoryTag,
  TagsRepository
>() {
}
