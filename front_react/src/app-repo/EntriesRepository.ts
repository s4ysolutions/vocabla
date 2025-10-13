import {Definition, type Entry} from '../domain/Entry.ts';
import {Context, Effect, Option} from 'effect';
import type {InfraError} from './InfraError.ts';
import type {Identifier} from '../domain/identity/Identifier.ts';
import type {Tag} from '../domain/Tag.ts';
import type {Student} from '../domain/Student.ts';
import type {EntriesFilter} from '../domain/EntriesFilter.ts';
import type {Identified} from '../domain/identity/Identified.ts';
import type {Localized} from '../domain/Localized.ts';

export interface EntriesRepository {
  createEntry: (
    studentId: Identifier<Student>,
    word: Localized, definitions: Readonly<Definition[]>,
    tagIds: Array<Identifier<Tag>>) => Effect.Effect<Identifier<Entry>, InfraError>
  getEntry: (
    studentId: Identifier<Student>,
    entryId: Identifier<Entry>) => Effect.Effect<Option.Option<Entry>, InfraError>
  getEntries: (
    studentId: Identifier<Student>,
    filter: EntriesFilter) => Effect.Effect<{
    readonly entries: ReadonlyArray<Identified<Entry>>
  }, InfraError>
  deleteEntry: (
    studentId: Identifier<Student>,
    entryId: Identifier<Entry>) => Effect.Effect<void, InfraError>
}

export class EntriesRepositoryTag extends Context.Tag('EntriesRepository')<
  EntriesRepositoryTag,
  EntriesRepository
>() {
}
