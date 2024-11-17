import React, { ReactElement } from 'react'
import PrimaryButton from '../../../react/widgets/buttons/PrimaryButton'
import Selector from '../../../react/widgets/selectors'
import useLearningLanguages from './hooks/useLearningLanguages'
import useIUnderstandLanguages from './hooks/useIUnderstandLanguages'


const EntryAdd: React.FC = (): ReactElement => {
    const { languages: ll, loading: lll } = useLearningLanguages()
    const learningLanguages = lll ? [] : ll.map(e => ({ id: e.id, value: e.flag + " " + e.name }))
    const selectedLL = learningLanguages.length > 0 ? learningLanguages[0].id : ''

    const { languages: ul, loading: ull } = useIUnderstandLanguages()
    const understandLanguages = ull ? [] : ul.map(e => ({ id: e.id, value: e.flag + " " + e.name }))
    const selectedUL = understandLanguages.length > 0 ? learningLanguages[0].id : ''

    return <React.Fragment>
        <div className="flex items-center space-x-4">

            <Selector values={learningLanguages} selectedId={selectedLL} />
            <input
                type="text"
                className="border border-gray-300 rounded px-2 py-1"
                autoFocus
            />
            {/* <ul className="mt-2 space-y-1"> </ul>*/}
            {/* Spacer */}
            <div className="flex-1" />
            <div className="ml-auto">
                <PrimaryButton>
                    Add New Entry
                </PrimaryButton>
            </div>
        </div>
        <br />
        <div className="flex items-center space-x-4">
            <Selector values={understandLanguages} selectedId={selectedUL} />
            <textarea
                className="flex-1 border border-gray-300 rounded px-2 py-1 resize-none"
                placeholder="Enter a description"
                rows={3}
            />
        </div>
    </React.Fragment>
}

export default EntryAdd