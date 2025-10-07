import React from 'react'
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

const renderLog = loglevel.getLogger('render')

const WordManager: React.FC = () => {
  renderLog.debug('Rendering WordManager component')
  const filter = entriesFilterEmpty
  const {entries, loading, addEntry} = useEntries(filter)
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
  </div>
}

export default WordManager
