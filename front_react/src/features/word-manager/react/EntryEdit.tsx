import React, { ReactElement, useState } from 'react'
import PrimaryButton from '../../../react/widgets/buttons/PrimaryButton'
import Entry from '../domain/models/entry'
import InputText from '../../../react/widgets/inputs/InputText'
import CancelButton from '../../../react/widgets/buttons/CancelButton copy'
import LanguageSelect from '../../../react/widgets/selectors/LanguageSelect'
import useLearningLanguages from './hooks/useLearningLanguages'
import useIUnderstandLanguages from './hooks/useIUnderstandLanguages'
import Lang from '../domain/models/lang'
import DefinitionEdit from './DefinitionEdit'

interface DefintionHolder {
  readonly id: number
  language: Lang
  s: string
}

const createDefinitionHolder = (language: Lang, s: string): DefintionHolder => ({
  id: Math.floor(Math.random() * 32000),
  language,
  s,
})

const arrangeDefinitionHolders = (language: Lang, defintionHolders: DefintionHolder[]): DefintionHolder[] => {
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

const EntryEdit: React.FC<Props> = ({ entry, onComplete }): ReactElement => {

  const { languages: learningLanguages, loading: lll } = useLearningLanguages()
  const [learningLanguage, setLearningLanguage] = useState(entry.lang)
  const [word, setWord] = useState<string>(entry.word)

  const { languages: languagesIUnderstand } = useIUnderstandLanguages()
  const languageIUnderstand = languagesIUnderstand ? languagesIUnderstand[0] : undefined

  const [definitions, setDefinitions] =
    useState<DefintionHolder[]>(
      languageIUnderstand
        ? arrangeDefinitionHolders(languageIUnderstand, entry.definitions
          .map((d) => createDefinitionHolder(d.localized.lang, d.localized.s)))
        : [])

  // derty hack to init the definition holders after the languageIUnderstand is loaded
  if (languageIUnderstand !== undefined && definitions.length === 0) {
    setDefinitions(arrangeDefinitionHolders(languageIUnderstand, []))
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
          defaultLanguage={entry.lang}
          loading={lll}
          onChange={(lang) => setLearningLanguage(lang)}
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
                onChangeDefinition={s => { def.s = s; setDefinitions(arrangeDefinitionHolders(languageIUnderstand, definitions)) }}
                defaultLanguage={def.language}
                onChangeLanguage={(lang) => def.language = lang}
              />
            )}
        </ul>
      </div>

      {/* Action Buttons */}
      <div className="flex justify-end space-x-2" id="action-buttons">
        <CancelButton onClick={onComplete} title='Cancel' />
        <PrimaryButton onClick={handleSave} title='Save' />
      </div>
    </div >
  )
}

export default EntryEdit