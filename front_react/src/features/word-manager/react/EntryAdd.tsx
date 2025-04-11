import React, { ReactElement } from 'react'
import PrimaryButton from '../../../react/widgets/buttons/PrimaryButton'
import useLearningLanguages from './hooks/useLearningLanguages'
import LanguageSelect from '../../../react/widgets/selectors/LanguageSelect'
import ProgressInfinity from '../../../react/widgets/progress-infinity'


const EntryAdd: React.FC = (): ReactElement => {
    const { learningLanguages, loading: lll } = useLearningLanguages()
    // const learningLanguages = lll ? [] : ll.map(e => ({ id: e.id, value: e.flag + " " + e.name }))
    // const selectedLL = learningLanguages.length > 0 ? learningLanguages[0].id : ''

    // const { languages: ul, loading: ull } = useIUnderstandLanguages()
    // const understandLanguages = ull ? [] : ul.map(e => ({ id: e.id, value: e.flag + " " + e.name }))
    // const selectedUL = understandLanguages.length > 0 ? learningLanguages[0].id : ''

    return <React.Fragment>
        <div className="flex items-center space-x-4">
            <LanguageSelect
                languages={learningLanguages}
                initialLanguage={learningLanguages[0]}
                loading={lll} />

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
            {/*
            <LanguageSelect languages={} selectedId={selectedUL} >
                <span className="block text-sm font-medium text-gray-700">Language</span>
            </LanguageSelect>
            */}
            <textarea
                className="flex-1 border border-gray-300 rounded px-2 py-1 resize-none"
                placeholder="Enter a description"
                rows={3}
            />
        </div>
    </React.Fragment>
}

export default EntryAdd