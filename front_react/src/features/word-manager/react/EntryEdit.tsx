import React, { ReactElement } from 'react'
import PrimaryButton from '../../../react/widgets/buttons/PrimaryButton'
import Entry from '../domain/models/entry'
import DefinitionEdit from './DefinitionEdit'
import Definition, { emptyDefinition } from '../domain/models/definition'
import Lang from '../domain/models/lang'

// Holders used to store the valus entered by the user
// They will be stored in underlyng models on save

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

interface Props {
  entry: Entry
  understandLanguage: Lang
  onSave: (updatedEntry: Entry) => void
  onCancel: () => void
}

const EntryEdit: React.FC<Props> = ({ entry, onSave, onCancel, understandLanguage }): ReactElement => {


  const handleSave = () => {
    // For now, just call onSave with the current entry
    onSave({
      ...entry,
      /*
      learningLanguage: selectedLL,
      understandLanguage: selectedUL,
      */
    })
  }
  const entryHolder = new EntryHolder(entry, understandLanguage)

  // const [word, setWord] = React.useState(entry.word)
  // const [wordLanguage, setWordLanguage] = React.useState(entry.lang)
  // const [definitions, setDefinitions] = React.useState(EntryHolder.definitions)

  // const { learningLanguages: ll, loading: lll } = useLearningLanguages()
  // const learningLanguages = lll ? [] : ll.map(e => ({ id: e.id, value: e.flag + " " + e.name }))

  return (
    <div className="space-y-4">
      {/* Language Selector and Word Input */}
      <div className="flex items-center space-x-4">
        {/* Word Input */}
        <div className="flex-1">
          <label className="block text-sm font-medium text-gray-700">Word</label>
          <input
            type="text"
            defaultValue={entryHolder.word}
            className="w-full border border-gray-300 rounded px-2 py-1"
          />
        </div>

        {/* Language Selector */}
        <div className="flex-1">
          {/* <Selector values={learningLanguages} defaultValue={entryHolder.learningLanguage.id} /> */}
        </div>
      </div>

      {/* Definitions */}
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
      </div>

      {/* Action Buttons */}
      <div className="flex justify-end space-x-2">
        <button
          onClick={onCancel}
          className="px-4 py-2 bg-gray-300 rounded hover:bg-gray-400"
        >
          Cancel
        </button>
        <PrimaryButton onClick={handleSave}>
          Save
        </PrimaryButton>
      </div>
    </div>
  )
}

export default EntryEdit