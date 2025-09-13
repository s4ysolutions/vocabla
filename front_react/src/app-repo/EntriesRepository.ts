import type {Entry} from '../domain/Entry.ts';
import {Context, Effect, Option} from 'effect';
import type {InfraError} from './infraError.ts';
import type {Identifier} from '../domain/identity/Identifier.ts';
import type {Tag} from '../domain/Tag.ts';

export interface EntriesRepository {
  createEntry: (entry: Entry, tagIds: Array<Identifier<Tag>>) => Effect.Effect<Identifier<Entry>, InfraError>
  getEntry: (entryId: Identifier<Entry>) => Effect.Effect<Option.Option<Entry>, InfraError>
}

export class EntriesRepositoryTag extends Context.Tag('EntriesRepository')<
  EntriesRepositoryTag,
  EntriesRepository
>() {
}
