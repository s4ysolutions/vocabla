import { useEffect, useState } from 'react';
import { runAppEffect } from '../../../../../app/effect-runtime.ts';
import type {Entry} from '../../../../../domain/Entry.ts';
import {Identifier} from '../../../../../domain/identity/Identifier.ts';
import type {Student} from '../../../../../domain/Student.ts';

const useEntries = (
  ownerId: string
): {
  entries: Array<Entry>;
  loading: boolean;
  addEntry: (
    word: string,
    wordLang: string,
    definition: string,
    definitionLang: string,
    tagLabels: string[]
  ) => Promise<void>;
} => {
  const [entries, setEntries] = useState<Array<Entry>>([]);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    const fetchEntries = async () => {
      setLoading(true);

      try {
        const entries = await runAppEffect(
          getEntriesUseCase(Identifier<Student>(ownerId))
        );
        setEntries(entries);
      } catch (error) {
        console.error('Failed to fetch entries:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchEntries();
  }, [ownerId]);

  const addEntry = async (
    word: string,
    wordLang: string,
    definition: string,
    definitionLang: string,
    tagLabels: string[]
  ) => {
    try {
      await runAppEffect(
        addEntryUseCase(
          id(ownerId),
          word,
          wordLang,
          definition,
          definitionLang,
          tagLabels
        )
      );

      const refreshedEntries = await runAppEffect(
        getEntriesUseCase(id(ownerId))
      );
      setEntries(refreshedEntries);
    } catch (error) {
      console.error('Failed to add entry:', error);
    }
  };

  return { entries, loading, addEntry };
};

export default useEntries;
