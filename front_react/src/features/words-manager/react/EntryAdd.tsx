import React, { ReactElement, useState } from 'react'
import PrimaryButton from '../../../react/widgets/buttons/PrimaryButton'
import useLearningLanguages from './hooks/useLearningLanguages'
import LanguageSelect from '../../../react/widgets/selectors/LanguageSelect'
import useIUnderstandLanguages from './hooks/useIUnderstandLanguages'
import InputText from '../../../react/widgets/inputs/InputText'
import Textarea from '../../../react/widgets/textarea/Textarea'

const EntryAdd: React.FC = (): ReactElement => {
    const { languages: learningLanguages, loading: lll } = useLearningLanguages()
    const { languages: understandLanguages, loading: lul } = useIUnderstandLanguages()
    const [word, setWord] = useState<string>('')
    const [definition, setDescription] = useState<string>('')
    const [learningLanguage, setLearningLanguage] = useState(learningLanguages[0])
    const [understandLanguage, setUnderstandLanguage] = useState(understandLanguages[0])

    // ugly hack to set the initial languages
    if (!learningLanguage && learningLanguages[0] !== undefined) {
        setLearningLanguage(learningLanguages[0])
    }
    if (!understandLanguage && understandLanguages[0] !== undefined) {
        setUnderstandLanguage(understandLanguages[0])
    }

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault()
        console.log({
            word,
            definition: definition,
            learningLanguage,
            understandLanguage,
        })
        // Add logic to handle form submission
    }

    return <form onSubmit={handleSubmit}>
        <div className="flex items-center space-x-4">
            <LanguageSelect
                languages={learningLanguages}
                defaultLanguage={learningLanguages[0]}
                loading={lll}
                onChange={(lang) => setLearningLanguage(lang)}
            />

            <InputText
                defaultValue={word}
                onChange={(e) => setWord(e.target.value)}
                type="text"
                autoFocus
            />
            {/* Spacer */}
            <div className="flex-1" />
            <PrimaryButton type="submit" title='Add New Entry'/>
        </div>
        <br />
        <div className="flex items-center space-x-4">
            <LanguageSelect
                languages={understandLanguages}
                defaultLanguage={learningLanguages[0]}
                loading={lul}
                onChange={(s) => setUnderstandLanguage(s)} />
            <Textarea
                placeholder="Enter a description"
                defaultValue={definition}
                onChange={(e) => setDescription(e.target.value)}
                rows={3}
            />
        </div>
    </form>
}

export default EntryAdd