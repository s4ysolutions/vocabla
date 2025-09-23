import {useEffect, useState} from 'react';
import {forkAppEffect, interruptFiber, promiseAppEffect} from '../../../../app/effect-runtime.ts';
import {Definition, Entry} from '../../../../domain/Entry.ts';
import {Identifier} from '../../../../domain/identity/Identifier.ts';
import type {Student} from '../../../../domain/Student.ts';
import {Effect, Exit} from 'effect';
import {type GetEntriesByOwnerResponse, GetEntriesByOwnerUseCaseTag} from '../../../../app-ports/entries/GetEntriesByOwner.ts';
import {Identified} from '../../../../domain/identity/Identified.ts';
import {entriesFilterEmpty} from '../../../../domain/EntriesFilter.ts';
import {CreateEntryRequest, CreateEntryUseCaseTag} from '../../../../app-ports/entries/CreateEntryUseCase.ts';
import {Localized} from '../../../../domain/Localized.ts';
import {LangCode} from '../../../../domain/LangCode.ts';
import type {AppError} from '../../../../app-ports/errors/AppError.ts';
import log from 'loglevel';
import type {Tag} from '../../../../domain/Tag.ts';

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
    return yield* useCase.createEntry(request).pipe(
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
    wordLang: LangCode,
    definition: string,
    definitionLang: LangCode,
    tagLabels: Identifier<Tag>[]
  ) => Promise<void>;
} => {
  const [entries, setEntries] = useState<ReadonlyArray<Identified<Entry>>>([]);
  const [loading, setLoading] = useState<boolean>(true);


  useEffect(() => {
    setLoading(true)
    log.debug('Fetching entries for ownerId:', ownerId);
    const fiber = forkAppEffect(programGetEntries(ownerId));
    fiber.addObserver(exit => {
      log.debug('Fetched entries for ownerId:', ownerId, 'with result:', exit);
      setEntries(Exit.getOrElse(exit, () => []))
      setLoading(false)
    })
    return () => interruptFiber(fiber);
  }, [ownerId]);

  //Effect.tap(() => Effect.log('Fetching entries...')),
  //Effect.tap(entries => Effect.log(`Fetched ${entries.length} entries`))

  const addEntry = async (
    word: string,
    wordLang: LangCode,
    definition: string,
    definitionLang: LangCode,
    tagLabels: Identifier<Tag>[]
  ) => {
    void tagLabels
    const createEntryRequest = CreateEntryRequest(
      Entry(Localized(wordLang, word), [Definition(Localized(definitionLang, definition))], ownerId),
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
