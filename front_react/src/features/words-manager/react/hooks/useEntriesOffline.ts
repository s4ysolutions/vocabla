import { useEffect, useState } from "react";
import Entry from "../../domain/models/entry";
import Lang from "../../../../domain/Lang.ts";
import Source from "../../domain/models/source";

const en: Lang = {
    code: "en",
    name: "English",
}

const sourceManual: Source = {
    title: "Manual",
}

const generateRandomEntries = (count: number): Array<Entry> => {
    const words = [
        "abandon", "ability", "able", "about", "above", "accept", "access", "accident",
        "account", "achieve", "acquire", "across", "action", "active", "activity", "actor",
        "actual", "adapt", "add", "address", "adjust", "admit", "adult", "advance", "advice",
        "affect", "afford", "after", "again", "against", "agency", "agent", "agree", "ahead",
        "allow", "almost", "alone", "along", "already", "also", "although", "always", "amaze",
        "among", "amount", "analysis", "ancient", "anger", "animal", "announce", "annual",
        "another", "answer", "anxiety", "anyone", "anything", "anyway", "apart", "apology",
        "appeal", "appear", "apply", "appoint", "approach", "approve", "argue", "arise",
    ];

    return Array.from({ length: count }, (_, i) => ({
        word: words[i % words.length], // Cycle through the words array
        lang: en,
        definitions: [
            {
                localized: {
                    s: `Definition of ${words[i % words.length]}`,
                    lang: en,
                },
                source: sourceManual,
            },
        ],
    }));
};

const mockVocab: Array<Entry> = generateRandomEntries(64);


const useEntries = ():{entries: Array<Entry>, loading: boolean} => {
    const [entries, setEntries] = useState<Array<Entry>>([]);
    const [loading, setLoading] = useState<boolean>(true);

    useEffect(() => {
      const fetchEntries = async () => {
        setLoading(true);
        await new Promise((resolve) => setTimeout(resolve, 1000)); // Simulate delay
        setEntries(mockVocab);
        setLoading(false);
      };

      fetchEntries();
    }, []);

    return { entries, loading };
  };

  export default useEntries;
