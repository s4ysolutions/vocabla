import { useEffect, useState } from "react";
import Lang from "../../domain/models/lang";
import { id } from "../../domain/models/id";

const mockLanguages: Array<Lang> = [
    { id: id("en"), name: "English", flag: "🇬🇧" },
    { id: id("es"), name: "Spanish", flag: "🇪🇸" },
    { id: id("fr"), name: "French", flag: "🇫🇷" },
    { id: id("de"), name: "German", flag: "🇩🇪" },
    { id: id("it"), name: "Italian", flag: "🇮🇹" },
    { id: id("pt"), name: "Portuguese", flag: "🇧🇷" },
    { id: id("zh"), name: "Chinese", flag: "🇨🇳" },
    { id: id("ja"), name: "Japanese", flag: "🇯🇵" },
    { id: id("ru"), name: "Russian", flag: "🇷🇺" },
    { id: id("ar"), name: "Arabic", flag: "🇸🇦" },
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