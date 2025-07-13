import { useEffect, useState } from "react";
import Entry from "../../domain/models/entry";
import { id } from "../../../../domain/id";
import getEntriesUseCase from "../../application/use_cases/getEntriesUseCase";
import addEntryUseCase from "../../application/use_cases/addEntryUseCase";
import { runAppEffect } from "../../../../infra/effect-runtime";

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
          getEntriesUseCase(id(ownerId))
        );
        setEntries(entries);
      } catch (error) {
        console.error("Failed to fetch entries:", error);
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
      console.error("Failed to add entry:", error);
    }
  };

  return { entries, loading, addEntry };
};

export default useEntries;
