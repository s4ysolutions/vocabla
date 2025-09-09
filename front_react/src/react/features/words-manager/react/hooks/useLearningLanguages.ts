import {useEffect, useState} from 'react';
import type {Lang} from '../../../../../domain/Lang.ts';
import {langCode} from '../../../../../domain/LangCode.ts';

const mockLanguages: Array<Lang> = [
  {code: langCode('en'), name: 'English', flag: '🇬🇧'},
  {code: langCode('es'), name: 'Spanish', flag: '🇪🇸'},
  {code: langCode('fr'), name: 'French', flag: '🇫🇷'},
  {code: langCode('de'), name: 'German', flag: '🇩🇪'},
  {code: langCode('it'), name: 'Italian', flag: '🇮🇹'},
  {code: langCode('pt'), name: 'Portuguese', flag: '🇧🇷'},
  {code: langCode('zh'), name: 'Chinese', flag: '🇨🇳'},
  {code: langCode('ja'), name: 'Japanese', flag: '🇯🇵'},
  {code: langCode('ru'), name: 'Russian', flag: '🇷🇺'},
  {code: langCode('ar'), name: 'Arabic', flag: '🇸🇦'},
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

  return {languages, loading};
};

export default useLearningLanguages;
