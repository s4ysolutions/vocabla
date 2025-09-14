import React from 'react'
import useEntries from './hooks/useEntriesEffect.ts'
import EntryCard from './EntryCard.tsx'
import ProgressInfinity from '../../../widgets/progress-infinity'
import EntryAdd from './EntryAdd.tsx'
import Panel from '../../../widgets/panels/Panel.tsx'

const WordManager: React.FC = () => {
  console.log('WordManager rendered');
  const { entries, loading } = useEntries('0')

  return <div className="h-full w-full flex flex-col">
    {/* Scrollable List */}
    <div className="flex-1 overflow-y-auto p-4 bg-gray-50">
      {loading ?
        <ProgressInfinity />
        :
        <div className="space-y-4">
          {entries.map((entry) => <EntryCard key={entry.word} entry={entry} />)}
        </div>
      }
    </div>
    {/* Footer */}
    <Panel  className="bg-gray-200 border-t border-gray-300">
      <EntryAdd />
    </Panel>
  </div>
}

export default WordManager
