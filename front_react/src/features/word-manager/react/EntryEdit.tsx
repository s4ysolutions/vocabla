import React, { ReactElement, useState } from 'react'
import PrimaryButton from '../../../react/widgets/buttons/PrimaryButton'
import Entry from '../domain/models/entry'
import InputText from '../../../react/widgets/inputs/InputText'
import CancelButton from '../../../react/widgets/buttons/CancelButton copy'
import LanguageSelect from '../../../react/widgets/selectors/LanguageSelect'
import useLearningLanguages from './hooks/useLearningLanguages'

// Holders used to store the valus entered by the user
// They will be stored in underlyng models on save
/*
class DefintionHolder {
  readonly origin: Definition
  readonly id: number
  language: Lang
  s: string

  constructor(origin: Definition) {
    this.id = Math.random()
    this.origin = origin
    this.language = origin.localized.lang
    this.s = origin.localized.s
  }
}

class EntryHolder {
  readonly origin: Entry
  word: string
  learningLanguage: Lang
  definitions: DefintionHolder[]
  understandLanguage: Lang
  constructor(origin: Entry, understandLanguage: Lang) {
    this.origin = origin
    this.word = origin.word
    this.learningLanguage = origin.lang
    this.understandLanguage = understandLanguage
    this.definitions = origin.definitions.map(def => new DefintionHolder(def))
    this.addDefinition()
  }

  addDefinition() {
    // can be only one empty definition
    const nonEmpty = this.definitions.filter(def => def.s !== '')
    this.definitions = [...nonEmpty, new DefintionHolder(emptyDefinition(this.understandLanguage))]
  }
  removeDefinition(def: DefintionHolder) {
    this.definitions = this.definitions.filter(d => d.id !== def.id)
  }
}
*/

interface Props {
  entry: Entry
  onComplete: () => void
}

const EntryEdit: React.FC<Props> = ({ entry, onComplete }): ReactElement => {

  const { languages: learningLanguages, loading: lll } = useLearningLanguages()
  const [learningLanguage, setLearningLanguage] = useState(entry.lang)
  const [word, setWord] = useState<string>(entry.word)

  // const [definition, setDefinition] = useState<string>(e)

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
    <div className="space-y-4">
      {/* Language Selector and Word Input */}
      <div className="flex items-center space-x-4">
        <LanguageSelect
          languages={learningLanguages}
          initialLanguage={entry.lang}
          loading={lll}
          onChange={(lang) => setLearningLanguage(lang)}
        />
        <InputText
          id="word"
          defaultValue={word}
          onChange={(e) => setWord(e.target.value)}
        />
      </div>

      {/* Definitions 
      <div>
        <label className="block text-sm font-medium text-gray-700">Definitions</label>
        <ul className="space-y-2">
          {entry.definitions.map((def, i) => (
            <DefinitionEdit
              key={i}
              definition={def}
              onUpdate={(updatedDefinition) => {
                // Handle the updated definition here
                console.log('Updated Definition:', updatedDefinition)
              }} />
          ))}
        </ul>
      </div> */}

      {/* Action Buttons */}
      <div className="flex justify-end space-x-2">
        <CancelButton onClick={onComplete} title='Cancel' />
        <PrimaryButton onClick={handleSave} title='Save' />
      </div>
    </div >
  )
}

export default EntryEdit