import type {Identifier} from '../domain/identity/Identifier.ts';
import type {Student} from '../domain/Student.ts';
import type {Tag} from '../domain/Tag.ts';
import type {InfraError} from './infraError.ts';
import type {Effect} from 'effect/Effect';

export interface TagsRepository {
  createTag(tag: Tag, ownerId: Identifier<Student>): Effect<Identifier<Tag>, InfraError>
}
