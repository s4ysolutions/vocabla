import React, { ReactElement } from 'react'
import PrimaryButton from '../../../react/widgets/buttons/PrimaryButton'
import Selector from '../../../react/widgets/selectors'
import useLearningLanguages from './hooks/useLearningLanguages'
import Entry from '../domain/models/entry'
import DefinitionEdit from './DefinitionEdit'

interface Props {
  entry: Entry
  onSave: (updatedEntry: Entry) => void
  onCancel: () => void
}

const EntryEdit: React.FC<Props> = ({ entry, onSave, onCancel }): ReactElement => {
  const { languages: ll, loading: lll } = useLearningLanguages()
  const learningLanguages = lll ? [] : ll.map(e => ({ id: e.id, value: e.flag + " " + e.name }))
  const selectedLL = learningLanguages.length > 0 ? learningLanguages[0].id : ''

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

  return (
    <div className="space-y-4">
      {/* Language Selector and Word Input */}
      <div className="flex items-center space-x-4">
        {/* Word Input */}
        <div className="flex-1">
          <label className="block text-sm font-medium text-gray-700">Word</label>
          <input
            type="text"
            defaultValue={entry.word}
            className="w-full border border-gray-300 rounded px-2 py-1"
          />
        </div>

        {/* Language Selector */}
        <div className="flex-1">
          <Selector values={learningLanguages} selectedId={selectedLL} />
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
              }}/>
            /*
            <li key={i} className="border border-gray-300 rounded p-2">
              <span className="text-gray-700">{def.localized.s}</span>
            </li>
            */
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