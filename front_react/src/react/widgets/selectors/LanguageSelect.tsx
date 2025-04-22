import React from "react";
import Select from ".";
import Lang from "../../../domain/lang";
import ProgressInfinity from "../progress-infinity";

interface props {
    loading?: boolean
    defaultLanguage?: Lang
    language?: Lang
    languages: Array<Lang>
    onChange?: (lang: Lang) => void
}

const langById = (languages: Array<Lang>, id: string | undefined) => id && languages.find(lang => lang.id === id) || undefined;
const langFormatted = (lang: Lang | undefined) => <span className="block text-sm font-medium text-gray-700">{lang?.flag || ' '}{lang?.name}</span>;

const LanguageSelect: React.FC<props> = ({ defaultLanguage: initialLanguage, languages, language, onChange, loading }) =>
    (loading || !languages || languages.length === 0)
        ? <ProgressInfinity />
        : (languages.length === 1)
            ? <LanguageSelect1 language={languages[0]} />
            : <LanguageSelectMany defaultLanguage={initialLanguage} languages={languages} language={language} onChange={onChange} />


const LanguageSelect1: React.FC<{ language: Lang }> = ({ language }) =>
    <span className="block text-sm font-medium text-gray-700">{langFormatted(language)}</span>


const LanguageSelectMany: React.FC<props> = ({ defaultLanguage: initialLanguage, languages, language, onChange }) => {

    const [selectedId, setSelectedId] = React.useState(language?.id);

    return <Select
        values={languages.map((lang) => ({ id: lang.id, value: lang.name }))}
        value={selectedId}
        defaultValue={initialLanguage?.id}
        onChange={(e) => {
            const selectedLang = langById(languages, e.target.value);
            if (selectedLang) {
                setSelectedId(selectedLang.id);
                onChange?.(selectedLang);
            }
        }}
    >
        <LanguageSelect1 language={langById(languages, selectedId) || languages[0]} />
    </Select>
}


export default LanguageSelect;