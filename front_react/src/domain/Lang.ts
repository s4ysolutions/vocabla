import {Schema} from 'effect';
import {isLangCode, LangCode, schemaLangCode} from './LangCode.ts';

export const schemaLang = Schema.Struct({
  code: schemaLangCode,
  name: Schema.String,
  flag: Schema.optional(Schema.String)
})

export type Lang = Schema.Schema.Type<typeof schemaLang>

export const Lang = (code: string | LangCode, name?: string, flag?: string): Lang =>
  (isLangCode(code) && name == undefined && flag == undefined)
    ? mockLanguages.find(l => l.code === code) || mockLanguages[0]!
    : name ? schemaLang.make({
      code: LangCode(code),
      name,
      flag
    }) : (() => {
      throw new Error('Name is required if code is not a valid LangCode');
    })();

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
