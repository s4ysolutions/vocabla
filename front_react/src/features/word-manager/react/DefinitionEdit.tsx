import React, { ReactElement, useState } from 'react'
import Selector from '../../../react/widgets/selectors'
import useIUnderstandLanguages from './hooks/useIUnderstandLanguages.ts'
import Definition from '../domain/models/definition.ts'
import { id } from '../domain/models/id.ts'

interface Props {
  definition: Definition
  onUpdate?: (updatedDefinition: Definition) => void
}

const DefinitionEdit: React.FC<Props> = ({ definition/*, onUpdate*/ }): ReactElement => {
  const [editedText, setEditedText] = useState(definition.localized.s)
  const [selectedLangId, setSelectedLangId] = useState(definition.localized.lang.id)

  const { languages, loading } = useIUnderstandLanguages()
  const languageOptions = loading
    ? []
    : languages.map((lang) => ({ id: lang.id, value: lang.flag + ' ' + lang.name }))

  const handleUpdate = () => {
    /*
    onUpdate({
      localized: {
        s: editedText,
        langId: selectedLangId,
      },
    })
      */
  }

  return (
    <div className="flex item-start space-y-4">
      {/* Textarea for Definition */}
      <textarea
        value={editedText}
        onChange={(e) => setEditedText(e.target.value)}
        // onBlur={handleUpdate} // Automatically update on blur
        className="w-full border border-gray-300 rounded px-2 py-1 resize-none"
        rows={3}
      />

      {/* Language Selector */}
      <Selector
        values={languageOptions}
        selectedId={selectedLangId}
        onChange={(selected) => {
          setSelectedLangId(id(selected.target.value))
          handleUpdate() // Update immediately when language changes
        }}
      />
    </div>
  )
}

export default DefinitionEdit