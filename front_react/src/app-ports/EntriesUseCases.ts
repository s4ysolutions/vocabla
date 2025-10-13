import type {Identifier} from '../domain/identity/Identifier.ts';
import type {Tag} from '../domain/Tag.ts';
import {Context, Effect} from 'effect';
import type {AppError} from './errors/AppError.ts';
import {Definition, type Entry} from '../domain/Entry.ts';
import type {EntriesFilter} from '../domain/EntriesFilter.ts';
import type {Identified} from '../domain/identity/Identified.ts';
import type {Localized} from '../domain/Localized.ts';

export interface EntriesUseCases {
  createEntry(word: Localized, definitions: Readonly<Definition[]>, tagIds: ReadonlyArray<Identifier<Tag>>): Effect.Effect<Identifier<Entry>, AppError>

  getEntriesByOwner(filter: EntriesFilter): Effect.Effect<ReadonlyArray<Identified<Entry>>, AppError>;

  deleteEntry(entryId: Identifier<Entry>): Effect.Effect<void, AppError>;
}

export class EntriesUseCasesTag extends Context.Tag('EntriesUseCasesTag')<
  EntriesUseCasesTag,
  EntriesUseCases
>() {
}
