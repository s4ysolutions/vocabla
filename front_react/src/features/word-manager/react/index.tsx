import React from 'react'
import MatchParent from '../../../react/layotus/match-parent'
import useEntries from './hooks/useEntries'
import EntryCard from './EntryCard'
import ProgressInfinity from '../../../react/widgets/progress-infinity'
import EntryAdd from './EntryAdd'

const WordManager: React.FC = () => {
  const { entries, loading } = useEntries()

  return <MatchParent>
    <div className="h-full w-full flex flex-col">
      {/* Header */}
      <h1 className="text-xl font-bold p-4 bg-gray-200">Word Manager</h1>

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
      <div className="p-4 bg-gray-200 border-t border-gray-300">
        <EntryAdd />
      </div>
    </div></MatchParent>
}

export default WordManager