import type {Lang} from '../../../../domain/Lang.ts';
import {LangCode} from '../../../../domain/LangCode.ts';
import {useState} from 'react';

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

const useLanguage = (): Lang => {
  const [lang,] = useState(mockLanguages[0]!);
  return lang;
}

export default useLanguage;
