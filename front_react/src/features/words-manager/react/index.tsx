import React from 'react'
import useEntries from './hooks/useEntries'
import EntryCard from './EntryCard'
import ProgressInfinity from '../../../react/widgets/progress-infinity'
import EntryAdd from './EntryAdd'
import Panel from '../../../react/widgets/panels/Panel'

const WordManager: React.FC = () => {
  const { entries, loading } = useEntries()

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