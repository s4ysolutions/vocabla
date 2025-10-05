import useAllLanguages from '../shared/hooks/useAllLanguages.ts';
import type {LangCode} from '../../../domain/LangCode.ts';

interface LanguagesSelectorProps {
  selected: Set<LangCode>; // Set of selected language IDs
  addLang: (code: LangCode) => void; // Function to toggle selection
  removeLang: (code: LangCode) => void; // Function to toggle selection
}

const LanguagesSelector = ({selected, addLang, removeLang}: LanguagesSelectorProps) => {
  const languages = useAllLanguages();

  const selectedLangs = languages.filter(lang => selected.has(lang.code))
    .sort((a, b) => a.name.localeCompare(b.name));
  const availableLangs = languages.filter(lang => !selected.has(lang.code))
    .sort((a, b) => a.name.localeCompare(b.name));

  const orderedLangs = [...selectedLangs, ...availableLangs];

  return <div className="space-y-2">
    {
      orderedLangs.map((lang) => (
        <div key={lang.code} className="flex items-center space-x-2">
          <input
            type="checkbox"
            checked={selected.has(lang.code)}
            onChange={(el) => el.target.checked
              ? addLang(lang.code)
              : removeLang(lang.code)}
            className="cursor-pointer"
          />
          <span>{lang.flag} {lang.name} </span>
        </div>
      ))}
  </div>
}

export default LanguagesSelector;
