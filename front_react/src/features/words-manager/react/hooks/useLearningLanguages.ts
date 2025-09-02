import { useEffect, useState } from "react";
import Lang from "../../../../domain/Lang.ts";

const mockLanguages: Array<Lang> = [
    { code: "en",  name: "English", flag: "🇬🇧" },
    { code: "es",  name: "Spanish", flag: "🇪🇸" },
    { code: "fr",  name: "French", flag: "🇫🇷" },
    { code: "de",  name: "German", flag: "🇩🇪" },
    { code: "it",  name: "Italian", flag: "🇮🇹" },
    { code: "pt",  name: "Portuguese", flag: "🇧🇷" },
    { code: "zh",  name: "Chinese", flag: "🇨🇳" },
    { code: "ja",  name: "Japanese", flag: "🇯🇵" },
    { code: "ru",  name: "Russian", flag: "🇷🇺" },
    { code: "ar",  name: "Arabic", flag: "🇸🇦" },
];

const useLearningLanguages = (): { languages: Array<Lang>, loading: boolean } => {
    const [languages, setEntries] = useState<Array<Lang>>([]);
    const [loading, setLoading] = useState<boolean>(true);

    useEffect(() => {
        const fetchEntries = async () => {
            setLoading(true);
            await new Promise((resolve) => setTimeout(resolve, 1000)); // Simulate delay
            setEntries(mockLanguages);
            setLoading(false);
        };

        fetchEntries();
    }, []);

    return { languages, loading };
};

export default useLearningLanguages;
