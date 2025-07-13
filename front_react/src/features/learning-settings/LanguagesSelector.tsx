import React from 'react';
import languages from '../../domain/languages';

interface LanguagesSelectorProps {
    selected: Set<string>; // Set of selected language IDs
    toggle: (id: string) => void; // Function to toggle selection
}

const LanguagesSelector: React.FC<LanguagesSelectorProps> = ({selected, toggle }) =>
    <div className="space-y-2" >
        {
            languages.map((lang) => (
                <div key={lang.code} className="flex items-center space-x-2" >
                    <input
                        type="checkbox"
                        checked={selected.has(lang.code)}
                        onChange={() => toggle(lang.code)}
                        className="cursor-pointer"
                    />
                    <span>{lang.flag} {lang.name} </span>
                </div>
            ))}
    </div>

export default LanguagesSelector;