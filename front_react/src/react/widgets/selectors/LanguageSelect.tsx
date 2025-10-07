import React from 'react';
import Select from '.';
import ProgressInfinity from '../progress-infinity';
import type {Lang} from '../../../domain/Lang.ts';
import type {LangCode} from '../../../domain/LangCode.ts';

interface props {
  loading?: boolean
  selectedCode: LangCode //| null
  languages: ReadonlyArray<Lang>
  onChange?: ((lang: Lang) => void) | undefined
}

const langFormatted = (lang: Lang) =>
  <span className="block text-sm font-medium text-gray-700">{lang?.flag || ' '}{lang?.name}</span>;

const LanguageSelect: React.FC<props> = ({languages, selectedCode, onChange, loading}) =>
  (loading || !languages || languages.length === 0)
    ? <ProgressInfinity/>
    : (languages.length === 1)
      ? <LanguageSelect1 language={languages[0]!}/>
      : <LanguageSelectMany
        // fallbackLanguage={fallbackLanguage}
        languages={languages}
        selectedCode={selectedCode}
        onChange={onChange}/>


const LanguageSelect1: React.FC<{ language: Lang }> = ({language}) =>
  <span className="block text-sm font-medium text-gray-700">{langFormatted(language)}</span>

const langByCode =
  (languages: ReadonlyArray<Lang>, code: string) =>
    code && languages.find(lang => lang.code === code) || languages[0]!

const LanguageSelectMany: React.FC<props> = ({
                                               // fallbackLanguage,
                                               languages,
                                               selectedCode,
                                               onChange
                                             }) => {

  const selectedLang = langByCode(languages, selectedCode);

  return <Select
    values={languages.map((lang) => ({id: lang.code, value: lang.name}))}
    value={selectedCode}
    onChange={(e) => {
      const selectedLang = langByCode(languages, e.target.value);
      onChange?.(selectedLang);
    }}
  >
    {
      selectedLang && <LanguageSelect1 language={selectedLang}/>
    }
  </Select>
}


export default LanguageSelect;
