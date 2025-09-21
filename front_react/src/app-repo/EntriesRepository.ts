import type {Entry} from '../domain/Entry.ts';
import {Context, Effect, Option} from 'effect';
import type {InfraError} from './infraError.ts';
import type {Identifier} from '../domain/identity/Identifier.ts';
import type {Tag} from '../domain/Tag.ts';
import type {Student} from '../domain/Student.ts';
import type {EntriesFilter} from '../domain/EntriesFilter.ts';
import type {Identified} from '../domain/identity/Identified.ts';

export interface EntriesRepository {
  createEntry: (entry: Entry, tagIds: Array<Identifier<Tag>>) => Effect.Effect<Identifier<Entry>, InfraError>
  getEntry: (entryId: Identifier<Entry>) => Effect.Effect<Option.Option<Entry>, InfraError>
  getEntriesByOwner: (ownerId: Identifier<Student>, filter: EntriesFilter) => Effect.Effect<{
    readonly entries: ReadonlyArray<Identified<Entry>>
  }, InfraError>
}

export class EntriesRepositoryTag extends Context.Tag('EntriesRepository')<
  EntriesRepositoryTag,
  EntriesRepository
>() {
}
