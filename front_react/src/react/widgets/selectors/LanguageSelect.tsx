import React from 'react';
import Select from '.';
import ProgressInfinity from '../progress-infinity';
import type {Lang} from '../../../domain/Lang.ts';

interface props {
  loading?: boolean
  defaultLanguage?: Lang | undefined
  language?: Lang | undefined
  languages: Array<Lang>
  onChange?: ((lang: Lang) => void) | undefined
}

const langByCode = (languages: Array<Lang>, code: string | undefined) => code && languages.find(lang => lang.code === code) || undefined;
const langFormatted = (lang: Lang | undefined) => <span
  className="block text-sm font-medium text-gray-700">{lang?.flag || ' '}{lang?.name}</span>;

const LanguageSelect: React.FC<props> = ({defaultLanguage: initialLanguage, languages, language, onChange, loading}) =>
  (loading || !languages || languages.length === 0)
    ? <ProgressInfinity/>
    : (languages.length === 1)
      ? <LanguageSelect1 language={languages[0]!}/>
      : <LanguageSelectMany
        defaultLanguage={initialLanguage}
        languages={languages}
        language={language}
        onChange={onChange}/>


const LanguageSelect1: React.FC<{ language: Lang }> = ({language}) =>
  <span className="block text-sm font-medium text-gray-700">{langFormatted(language)}</span>


const LanguageSelectMany: React.FC<props> = ({defaultLanguage: initialLanguage, languages, language, onChange}) => {

  const [selectedId, setSelectedId] = React.useState(language?.code);
  const selectedLang = langByCode(languages, selectedId);

  return <Select
    values={languages.map((lang) => ({id: lang.code, value: lang.name}))}
    value={selectedId}
    defaultValue={initialLanguage?.code}
    onChange={(e) => {
      const selectedLang = langByCode(languages, e.target.value);
      if (selectedLang) {
        setSelectedId(selectedLang.code);
        onChange?.(selectedLang);
      }
    }}
  >
    {
      selectedLang && <LanguageSelect1 language={selectedLang}/>
    }
  </Select>
}


export default LanguageSelect;
