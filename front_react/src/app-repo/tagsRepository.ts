import type {Identifier} from '../domain/identity/Identifier.ts';
import type {Student} from '../domain/Student.ts';
import type {Tag} from '../domain/Tag.ts';

interface TagsRepository {
  createTag(tag: Tag, ownerId: Identifier<Student>): Promise<void>;
}
