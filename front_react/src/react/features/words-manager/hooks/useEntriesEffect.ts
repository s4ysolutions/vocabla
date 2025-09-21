import {useEffect, useState} from 'react';
import {forkAppEffect, interruptFiber, promiseAppEffect} from '../../../../app/effect-runtime.ts';
import {Definition, Entry} from '../../../../domain/Entry.ts';
import {Identifier} from '../../../../domain/identity/Identifier.ts';
import type {Student} from '../../../../domain/Student.ts';
import {Effect} from 'effect';
import {type GetEntriesByOwnerResponse, GetEntriesByOwnerUseCaseTag} from '../../../../app-ports/GetEntriesByOwner.ts';
import {Identified} from '../../../../domain/identity/Identified.ts';
import {entriesFilterEmpty} from '../../../../domain/EntriesFilter.ts';
import {CreateEntryRequest, CreateEntryUseCaseTag} from '../../../../app-ports/CreateEntryUseCase.ts';
import {Localized} from '../../../../domain/Localized.ts';
import {LangCode} from '../../../../domain/LangCode.ts';
import type {AppError} from '../../../../app-ports/errors/AppError.ts';

const programGetEntries =
  (ownerId: Identifier<Student>): Effect.Effect<ReadonlyArray<Identified<Entry>>, never, GetEntriesByOwnerUseCaseTag> =>
    Effect.gen(function* () {
      const useCase = yield* GetEntriesByOwnerUseCaseTag
      const {entries} = yield* useCase.getEntriesByOwner({
        ownerId,
        filter: entriesFilterEmpty
      }).pipe(
        Effect.catchAll((error) =>
          Effect.log('Application error fetching entries: ' + error.message).pipe(
            Effect.map(() =>
              ({entries: []} as GetEntriesByOwnerResponse)
            )
          )
        ));
      return entries
    })

const programAddEntry = (request: CreateEntryRequest): Effect.Effect<ReadonlyArray<Identified<Entry>>, never, CreateEntryUseCaseTag | GetEntriesByOwnerUseCaseTag> =>
  Effect.gen(function* () {
    const useCase = yield* CreateEntryUseCaseTag
    return yield * useCase.createEntry(request).pipe(
      Effect.flatMap(() => programGetEntries(request.entry.ownerId)),
      Effect.catchAll((error: AppError) =>
        Effect.log('Application error creating entry: ' + error.message).pipe(
          Effect.map(() =>
            ([] as ReadonlyArray<Identified<Entry>>)
          )
        )
      ))
  })

const useEntries = (
  ownerId: Identifier<Student>
): {
  entries: ReadonlyArray<Identified<Entry>>;
  loading: boolean;
  addEntry: (
    word: string,
    wordLang: string,
    definition: string,
    definitionLang: string,
    tagLabels: string[]
  ) => Promise<void>;
} => {
  const [entries, setEntries] = useState<ReadonlyArray<Identified<Entry>>>([]);
  const [loading, setLoading] = useState<boolean>(true);


  useEffect(() => {
    setLoading(true)
    const fiber = forkAppEffect(programGetEntries(ownerId).pipe(
      Effect.map(
        entries => {
          setEntries(entries);
          setLoading(false)
        })
    ));
    return () => interruptFiber(fiber);
  }, [ownerId]);

  const addEntry = async (
    word: string,
    wordLang: string,
    definition: string,
    definitionLang: string,
    tagLabels: string[]
  ) => {
    void tagLabels
    const createEntryRequest = CreateEntryRequest(
      Entry(Localized(LangCode(wordLang), word), [Definition(Localized(LangCode(definitionLang), definition))], ownerId),
      []
    )
    try {
      const refreshedEntries = await promiseAppEffect(programAddEntry(createEntryRequest));
      setEntries(refreshedEntries.map(e => e));
    } catch (error) {
      console.error('Failed to add entry:', error);
    }
  };

  return {entries, loading, addEntry};
};

export default useEntries;
