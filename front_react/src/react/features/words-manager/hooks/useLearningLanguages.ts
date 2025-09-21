import {useEffect, useState} from 'react';
import type {Lang} from '../../../../domain/Lang.ts';
import {LangCode} from '../../../../domain/LangCode.ts';

const mockLanguages: Array<Lang> = [
  {code: LangCode('en'), name: 'English', flag: '🇬🇧'},
  {code: LangCode('es'), name: 'Spanish', flag: '🇪🇸'},
  {code: LangCode('fr'), name: 'French', flag: '🇫🇷'},
  {code: LangCode('de'), name: 'German', flag: '🇩🇪'},
  {code: LangCode('it'), name: 'Italian', flag: '🇮🇹'},
  {code: LangCode('pt'), name: 'Portuguese', flag: '🇧🇷'},
  {code: LangCode('zh'), name: 'Chinese', flag: '🇨🇳'},
  {code: LangCode('ja'), name: 'Japanese', flag: '🇯🇵'},
  {code: LangCode('ru'), name: 'Russian', flag: '🇷🇺'},
  {code: LangCode('ar'), name: 'Arabic', flag: '🇸🇦'},
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
