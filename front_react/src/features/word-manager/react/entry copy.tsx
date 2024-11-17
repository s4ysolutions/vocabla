import React, { ReactElement, useState } from 'react'
import Entry from '../domain/models/entry'

interface Props {
    entry: Entry
}

const EntryCardCopy: React.FC<Props> = ({ entry }): ReactElement => {
    const [editedWord, setEditedWord] = useState(entry.word)
    const [isEditingWord, setIsEditingWord] = useState(false)

    const handleWordBlur = () => {
        setIsEditingWord(false)
        if (editedWord !== entry.word) {
            console.log('Word changed from', entry.word, 'to', editedWord)
            // onUpdate({ ...entry, word: editedWord })
        }
    }

    return (
        <div className="p-4 bg-white shadow rounded-lg border border-gray-200" >
            <h2 className="text-lg font-semibold">
                {isEditingWord ? (
                    <input
                        type="text"
                        value={editedWord}
                        onChange={(e) => setEditedWord(e.target.value)}
                        onBlur={handleWordBlur}
                        className="border border-gray-300 rounded px-2 py-1"
                        autoFocus
                    />
                ) : (
                    <span
                        tabIndex={0} // Makes the span focusable
                        onFocus={() => setIsEditingWord(true)}
                        className="cursor-pointer focus:outline-none"
                    >
                        {entry.word}
                    </span>
                )}
            </h2>
            <ul className="mt-2 space-y-1">
                {entry.definitions.map((def, i) => (
                    <li key={i} className="text-gray-700">
                        {def.localized.s}
                    </li>
                ))}
            </ul>
        </div>
    )
}

export default EntryCardCopy