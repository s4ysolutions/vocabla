import {type EntriesUseCases, EntriesUseCasesTag} from '../app-ports/EntriesUseCases.ts';
import type {Localized} from '../domain/Localized.ts';
import {type Definition, Entry} from '../domain/Entry.ts';
import {AppError} from '../app-ports/errors/AppError.ts';
import type {EntriesFilter} from '../domain/EntriesFilter.ts';
import type {Identified} from '../domain/identity/Identified.ts';
import {Effect, Layer} from 'effect';
import type {Tag} from '../domain/Tag.ts';
import {type EntriesRepository, EntriesRepositoryTag} from '../app-repo/EntriesRepository.ts';
import type {Identifier} from '../domain/identity/Identifier.ts';
import {type MeUseCases, MeUseCasesTag} from '../app-ports/MeUseCases.ts';
import infra2appError from './infra2appError.ts';
import {tt} from '../translable/Translatable.ts';

class EntriesUseCasesLive implements EntriesUseCases {
  private constructor(private readonly entriesRepository: EntriesRepository,
                      private readonly meUseCases: MeUseCases) {
  }

  static make(entriesRepository: EntriesRepository, meUseCases: MeUseCases): EntriesUseCases {
    return new EntriesUseCasesLive(entriesRepository, meUseCases)
  }

  static readonly layer = Layer.effect(
    EntriesUseCasesTag,
    Effect.all([
      EntriesRepositoryTag,
      MeUseCasesTag,
    ]).pipe(
      Effect.map(([entriesRepository, meUseCases]) => EntriesUseCasesLive.make(entriesRepository, meUseCases))
    ))

  createEntry(word: Localized, definitions: Readonly<Definition[]>, tagIds: Array<Identifier<Tag>>): Effect.Effect<Identifier<Entry>, AppError> {
    return this.meUseCases.currentStudentId.pipe(
      Effect.flatMap((aStudentId) => {
        if (aStudentId._state !== 'success') {
          return Effect.fail(AppError(tt`User not logged in`))
        }

        const entry = Entry(word, definitions, aStudentId.data)
        return this.entriesRepository.createEntry(entry, tagIds).pipe(
          Effect.mapError(infra2appError)
        )
      })
    )
  }

  getEntriesByOwner(filter: EntriesFilter): Effect.Effect<ReadonlyArray<Identified<Entry>>, AppError> {
    return this.meUseCases.currentStudentId.pipe(
      Effect.flatMap((aStudentId) => {
        if (aStudentId._state !== 'success') {
          return Effect.fail(AppError(tt`User not logged in`))
        }

        const studentId = aStudentId.data

        return this.entriesRepository.getEntriesByOwner(studentId, filter).pipe(
          Effect.mapError(infra2appError),
          Effect.map(r => r.entries)
        )
      })
    )
  }
}

export default EntriesUseCasesLive
