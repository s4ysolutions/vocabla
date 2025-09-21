import React, {type ReactElement, useState} from 'react'
import PrimaryButton from '../../widgets/buttons/PrimaryButton.tsx'
import InputText from '../../widgets/inputs/InputText.tsx'
import CancelButton from '../../widgets/buttons/CancelButton copy.tsx'
import LanguageSelect from '../../widgets/selectors/LanguageSelect.tsx'
import useLearningLanguages from './hooks/useLearningLanguages.ts'
import useIUnderstandLanguages from './hooks/useIUnderstandLanguages.ts'
import DefinitionEdit from './DefinitionEdit.tsx'
import type {Entry} from '../../../domain/Entry.ts';
import type {LangCode} from '../../../domain/LangCode.ts';
import useDefaultLanguage from '../shared/hooks/useDefaultLanguage.ts';
import {Lang} from '../../../domain/Lang.ts';

interface DefintionHolder {
  readonly id: number
  language: LangCode
  s: string
}

const createDefinitionHolder = (language: LangCode, s: string): DefintionHolder => ({
  id: Math.floor(Math.random() * 32000),
  language,
  s,
})

const arrangeDefinitionHolders = (language: LangCode, defintionHolders: DefintionHolder[]): DefintionHolder[] => {
  if (defintionHolders.length === 0) {
    return [createDefinitionHolder(language, '')]
  }

  const hole = defintionHolders.findIndex((d) => d.s.trim() === '')

  if (hole === defintionHolders.length - 1) {
    return defintionHolders
  }

  if (hole === -1) {
    return defintionHolders.concat(createDefinitionHolder(language, ''))
  }

  return defintionHolders.filter((d) => d.s.trim() !== '').concat(createDefinitionHolder(language, ''))
}

interface Props {
  entry: Entry
  onComplete: () => void
}

const EntryEdit: React.FC<Props> = ({entry, onComplete}): ReactElement => {

  const {languages: learningLanguages, loading: lll} = useLearningLanguages()
  const [learningLanguage, setLearningLanguage] = useState(entry.word.langCode)
  const defaultLanguage = useDefaultLanguage()
  const [word, setWord] = useState<string>(entry.word.s)
  const wordLang = Lang(entry.word.langCode)

  const {languages: languagesIUnderstand} = useIUnderstandLanguages()
  const languageIUnderstand = languagesIUnderstand ? languagesIUnderstand[0] : defaultLanguage

  const [definitions, setDefinitions] =
    useState<DefintionHolder[]>(
      languageIUnderstand
        ? arrangeDefinitionHolders(languageIUnderstand.code, entry.definitions
          .map((d) => createDefinitionHolder(d.localized.langCode, d.localized.s)))
        : [])

  // derty hack to init the definition holders after the languageIUnderstand is loaded
  if (languageIUnderstand !== undefined && definitions.length === 0) {
    setDefinitions(arrangeDefinitionHolders(languageIUnderstand.code, []))
  }

  const handleSave = () => {
    console.log({
      word,
      learningLanguage,
      // definition: definition,
      // learningLanguage,
      // understandLanguage,
    })
    onComplete()
  }

  return (
    <div className="space-y-4" id="entry-edit">
      {/* Language Selector and Word Input */}
      <div className="flex items-center space-x-4" id="word-input">
        <LanguageSelect
          languages={learningLanguages}
          defaultLanguage={wordLang}
          loading={lll}
          onChange={(lang) => setLearningLanguage(lang.code)}
        />
        <InputText
          id="word"
          defaultValue={word}
          onChange={(e) => setWord(e.target.value)}
        />
      </div>

      <div className="overflow-y-auto  max-h-full" id="definition-edit">
        <label className="block text-sm font-medium text-gray-700">Definitions</label>
        <ul className="space-y-2">
          {languageIUnderstand === undefined
            ? <p>Loading...</p>
            : definitions.map((def) =>
              <DefinitionEdit
                key={def.id}
                languagesIUnderstand={languagesIUnderstand}
                defaultDefinition={def.s}
                onChangeDefinition={s => {
                  def.s = s;
                  setDefinitions(arrangeDefinitionHolders(languageIUnderstand.code, definitions))
                }}
                defaultLanguage={Lang(def.language)}
                onChangeLanguage={(lang) => def.language = lang.code}
              />
            )}
        </ul>
      </div>

      {/* Action Buttons */}
      <div className="flex justify-end space-x-2" id="action-buttons">
        <CancelButton onClick={onComplete} title='Cancel'/>
        <PrimaryButton onClick={handleSave} title='Save'/>
      </div>
    </div>
  )
}

export default EntryEdit
