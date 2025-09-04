import type {TagsRepository} from '../app-repo/TagsRepository.ts';
import type {Tag} from '../domain/Tag.ts';
import type {Identifier} from '../domain/identity/Identifier.ts';
import type {Student} from '../domain/Student.ts';
import type {Effect} from 'effect/Effect';
import type {InfraError} from '../app-repo/infraError.ts';

const RestRepositories: TagsRepository = {
  createTag: (tag: Tag, ownerId: Identifier<Student>): Effect<Identifier<Tag>, InfraError> => {
    throw new Error('Function not implemented.');
  }
}

