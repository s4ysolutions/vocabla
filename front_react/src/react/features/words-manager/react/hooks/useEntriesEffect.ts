import {useEffect, useState} from 'react';
import {promiseAppEffect} from '../../../../../app/effect-runtime.ts';
import type {Entry} from '../../../../../domain/Entry.ts';
import {Identifier} from '../../../../../domain/identity/Identifier.ts';
import type {Student} from '../../../../../domain/Student.ts';
import {Effect} from 'effect';
import {GetEntriesByOwnerUseCaseTag} from '../../../../../app-ports/GetEntriesByOwner.ts';
import {Identified} from '../../../../../domain/identity/Identified.ts';
import {entriesFilterEmpty} from '../../../../../domain/EntriesFilter.ts';

const programGetEntries = (ownerId: Identifier<Student>) => Effect.gen(function* () {
  const useCase = yield* GetEntriesByOwnerUseCaseTag
  const {entries} = yield* useCase
    .getEntriesByOwner({ownerId, filter: entriesFilterEmpty})
    .pipe(
      Effect.catchAll(error => {
        // TODO: better error handling
        Effect.log('Application error fetching entries: ' + error.message)
        return Effect.succeed({entries: []})
      }),
    )
  return entries
})

const useEntries = (
  ownerId: Identifier<Student>
): {
  entries: Array<Identified<Entry>>;
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
    promiseAppEffect(programGetEntries(ownerId)).then(entries => {
        setEntries(entries);
        setLoading(false)
      }
    );
  }, [ownerId]);

  const addEntry = async (
    word: string,
    wordLang: string,
    definition: string,
    definitionLang: string,
    tagLabels: string[]
  ) => {
    try {
      await promiseAppEffect(
        addEntryUseCase(
          id(ownerId),
          word,
          wordLang,
          definition,
          definitionLang,
          tagLabels
        )
      );

      const refreshedEntries = await promiseAppEffect(
        getEntriesUseCase(id(ownerId))
      );
      setEntries(refreshedEntries);
    } catch (error) {
      console.error('Failed to add entry:', error);
    }
  };

  return {entries, loading, addEntry};
};

export default useEntries;
