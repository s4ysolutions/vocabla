import {useEffect, useState} from 'react';
import {forkAppEffect, interruptFiber, promiseAppEffect} from '../../../../app/effect-runtime.ts';
import {Definition, Entry} from '../../../../domain/Entry.ts';
import {Identifier} from '../../../../domain/identity/Identifier.ts';
import {Effect, Exit} from 'effect';
import {Identified} from '../../../../domain/identity/Identified.ts';
import {Localized} from '../../../../domain/Localized.ts';
import {LangCode} from '../../../../domain/LangCode.ts';
import type {AppError} from '../../../../app-ports/errors/AppError.ts';
import type {Tag} from '../../../../domain/Tag.ts';
import {EntriesUseCasesTag} from '../../../../app-ports/EntriesUseCases.ts';
import {type EntriesFilter} from '../../../../domain/EntriesFilter.ts';
import loglevel from 'loglevel';

const log = loglevel.getLogger('useEntriesEffect')
log.setLevel(loglevel.levels.INFO)

const programGetEntries = (
  filter: EntriesFilter
): Effect.Effect<ReadonlyArray<Identified<Entry>>, never, EntriesUseCasesTag> =>
  EntriesUseCasesTag.pipe(
    Effect.flatMap((useCase) => useCase.getEntriesByOwner(filter).pipe(
      Effect.catchAll((error) =>
        Effect.log('Application error fetching entries: ' + error.message).pipe(
          Effect.map(() => [] as ReadonlyArray<Identified<Entry>>)
        )
      ))
    )
  )

const programAddEntry = (
  word: Localized,
  definitions: ReadonlyArray<Definition>,
  tags: ReadonlyArray<Identifier<Tag>>,
  filter: EntriesFilter
): Effect.Effect<ReadonlyArray<Identified<Entry>>, never, EntriesUseCasesTag> =>
  EntriesUseCasesTag.pipe(
    Effect.flatMap((entriesUseCases) =>
      entriesUseCases.createEntry(word, definitions, tags).pipe(
        Effect.flatMap(() => programGetEntries(filter)),
        Effect.catchAll((error: AppError) =>
          Effect.log('Application error creating entry: ' + error.message).pipe(
            Effect.map(() =>
              ([] as ReadonlyArray<Identified<Entry>>)
            )
          )
        )
      )
    )
  )

const programDeleteEntry = (
  entryId: Identifier<Entry>,
  filter: EntriesFilter
): Effect.Effect<ReadonlyArray<Identified<Entry>>, never, EntriesUseCasesTag> =>
  EntriesUseCasesTag.pipe(
    Effect.flatMap((entriesUseCases) =>
      entriesUseCases.deleteEntry(entryId).pipe(
        Effect.flatMap(() => programGetEntries(filter)),
        Effect.catchAll((error: AppError) =>
          Effect.log('Application error deleting entry: ' + error.message).pipe(
            Effect.map(() =>
              ([] as ReadonlyArray<Identified<Entry>>)
            )
          )
        )
      )
    )
  )

const useEntries = (filter: EntriesFilter): {
  entries: ReadonlyArray<Identified<Entry>>;
  loading: boolean;
  addEntry: (
    word: string,
    wordLang: LangCode,
    definition: string,
    definitionLang: LangCode,
    tagIds: ReadonlyArray<Identifier<Tag>>,
    filter: EntriesFilter
  ) => Promise<void>,
  deleteEntry: (entryId: Identifier<Entry>, filter: EntriesFilter) => Promise<void>,
} => {
  const [entries, setEntries] = useState<ReadonlyArray<Identified<Entry>>>([]);
  const [loading, setLoading] = useState<boolean>(true);


  useEffect(() => {
    setLoading(true)
    const fiber = forkAppEffect(programGetEntries(filter));
    fiber.addObserver(exit => {
      setEntries(Exit.getOrElse(exit, () => []))
      setLoading(false)
    })
    return () => interruptFiber(fiber);
  }, [filter]);

  //Effect.tap(() => Effect.log('Fetching entries...')),
  //Effect.tap(entries => Effect.log(`Fetched ${entries.length} entries`))

  const addEntry = async (
    word: string,
    wordLang: LangCode,
    definition: string,
    definitionLang: LangCode,
    tagIds: ReadonlyArray<Identifier<Tag>>,
    filter: EntriesFilter
  ) => {
    log.debug('Adding entry:', {word, wordLang, definition, definitionLang, tagIds, filter});
    setLoading(true);
    try {
      const refreshedEntries = await promiseAppEffect(
        programAddEntry(Localized(wordLang, word), [Definition(Localized(definitionLang, definition))], tagIds, filter));
      setEntries(refreshedEntries.map(e => e));
    } catch (error) {
      log.error('Failed to add entry:', error);
    } finally {
      setLoading(false);
    }
  };

  const deleteEntry = async (entryId: Identifier<Entry>, filter: EntriesFilter) => {
    log.debug('Deleting entry:', {entryId});
    setLoading(true);
    try {
      const refreshedEntries = await promiseAppEffect(
        programDeleteEntry(entryId, filter));
      setEntries(refreshedEntries.map(e => e));
    } catch (error) {
      log.error('Failed to delete entry:', error);
    } finally {
      setLoading(false);
    }
  };

  return {entries, loading, addEntry, deleteEntry};
};

export default useEntries;
