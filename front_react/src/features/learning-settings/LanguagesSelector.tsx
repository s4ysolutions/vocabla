import React from 'react';
import { Id } from '../../domain/id';
import languages from '../../domain/languages';

interface LanguagesSelectorProps {
    selected: Set<Id>; // Set of selected language IDs
    toggle: (id: Id) => void; // Function to toggle selection
}

const LanguagesSelector: React.FC<LanguagesSelectorProps> = ({selected, toggle }) =>
    <div className="space-y-2" >
        {
            languages.map((lang) => (
                <div key={lang.id} className="flex items-center space-x-2" >
                    <input
                        type="checkbox"
                        checked={selected.has(lang.id)}
                        onChange={() => toggle(lang.id)}
                        className="cursor-pointer"
                    />
                    <span>{lang.flag} {lang.name} </span>
                </div>
            ))}
    </div>

export default LanguagesSelector;