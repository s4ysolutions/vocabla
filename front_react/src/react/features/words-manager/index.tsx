import React, {useState} from 'react'
import useEntries from './hooks/useEntriesEffect.ts'
import EntryCard from './EntryCard.tsx'
import ProgressInfinity from '../../widgets/progress-infinity'
import EntryAdd from './EntryAdd.tsx'
import Panel from '../../widgets/panels/Panel.tsx'
import loglevel from 'loglevel';
import {entriesFilterEmpty} from '../../../domain/EntriesFilter.ts';
import type {LangCode} from '../../../domain/LangCode.ts';
import type {Identifier} from '../../../domain/identity/Identifier.ts';
import type {Tag} from '../../../domain/Tag.ts';
import type {TagSmall} from '../../../domain/TagSmall.ts';
import type {Entry} from '../../../domain/Entry.ts';

const renderLog = loglevel.getLogger('render')

const WordManager: React.FC = () => {
  renderLog.debug('Rendering WordManager component')
  const filter = entriesFilterEmpty
  const {entries, loading, addEntry, removeEntry} = useEntries(filter)
  const [isDeletePopupOpen, setIsDeletePopupOpen] = useState(false);
  const [entryToDelete, setEntryToDelete] = useState<Identifier<Entry>| null>(null);
  /*
  const entries = []
  const loading = false
  const addEntry = async (
    word: string,
    wordLC: LangCode,
    definition: string,
    definitionLC: LangCode,
    tagIds: ReadonlyArray<Identifier<Tag>>,
    filter: (e: {word: string, wordLC: LangCode, definition: string, definitionLC: LangCode, tagIds: ReadonlyArray<Identifier<Tag>>}) => boolean
  ): Promise<void> => {
    // Mock implementation of adding an entry
    renderLog.debug('Adding entry:', {word, wordLC, definition, definitionLC, tagIds})
    return new Promise((resolve) => setTimeout(resolve, 500))
  }*/
  const addEntryFilter = (
    word: string,
    wordLC: LangCode,
    definition: string,
    definitionLC: LangCode,
    tagIds: ReadonlyArray<Identifier<Tag>>) => addEntry(
    word,
    wordLC,
    definition,
    definitionLC,
    tagIds,
    filter
  )

  const deleteEntry = () => {
    if (entryToDelete) {
      removeEntry(entryToDelete);
      setEntryToDelete(null);
      setIsDeletePopupOpen(false);
    }
  };

  return <div className="h-full w-full flex flex-col">
    {/* Scrollable List */}
    <div className="flex-1 overflow-y-auto p-4 bg-gray-50">
      {loading ?
        <ProgressInfinity/>
        :
        <div className="space-y-4">
          {entries.map((entry) => <EntryCard key={entry.id} entry={entry.e}/>)}
        </div>
      }
    </div>
    {/* Footer */}
    <Panel className="bg-gray-200 border-t border-gray-300">
      <EntryAdd add={addEntryFilter}/>
    </Panel>

    {/* Delete Confirmation Popup */}
    {isDeletePopupOpen && (
      <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
        <div className="bg-white p-6 rounded shadow-lg w-96">
          <h2 className="text-lg font-bold mb-4">Confirm Deletion</h2>
          <p>Are you sure you want to delete the entry "{entryToDelete}"?</p>
          <div className="flex justify-end space-x-2 mt-4">
            <button
              className="px-4 py-2 bg-gray-300 rounded hover:bg-gray-400"
              onClick={() => setIsDeletePopupOpen(false)}
            >
              Cancel
            </button>
            <button
              className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600"
              onClick={deleteEntry}
            >
              Delete
            </button>
          </div>
        </div>
      </div>
    )}
  </div>
}

export default WordManager
