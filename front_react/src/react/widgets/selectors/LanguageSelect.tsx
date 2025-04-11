import React from "react";
import Select from ".";
import Lang from "../../../features/word-manager/domain/models/lang";
import ProgressInfinity from "../progress-infinity";

interface props {
    loading?: boolean
    initialLanguage?: Lang
    language?: Lang
    languages: Array<Lang>
    onChange?: (lang: Lang) => void
}

const langById = (languages: Array<Lang>, id: string | undefined) => id && languages.find(lang => lang.id === id) || undefined;
const langFormatted = (lang: Lang | undefined) => <span className="block text-sm font-medium text-gray-700">{lang?.flag || ' '}{lang?.name}</span>;

const LanguageSelect: React.FC<props> = ({ initialLanguage, languages, language, onChange, loading }) =>
    (loading || !languages || languages.length === 0)
        ? <ProgressInfinity />
        : (languages.length === 1)
            ? <LanguageSelect1 language={languages[0]} />
            : <LanguageSelectMany initialLanguage={initialLanguage} languages={languages} language={language} onChange={onChange} />


const LanguageSelect1: React.FC<{ language: Lang }> = ({ language }) => <React.Fragment>
    <span className="block text-sm font-medium text-gray-700">{langFormatted(language)}</span>
    <span className="absolute inset-y-0 right-0 flex items-center pr-2 pointer-events-none">
        <svg className="w-5 h-5 text-gray-400" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 10l5 5 5-5H7z" />
        </svg>
    </span>
</React.Fragment>


const LanguageSelectMany: React.FC<props> = ({ initialLanguage, languages, language, onChange }) => {

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