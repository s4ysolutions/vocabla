import React, {type JSX, useState} from 'react'
import PrimaryButton from '../../widgets/buttons/PrimaryButton.tsx'
import useLearningLanguages from './hooks/useLearningLanguages.ts'
import LanguageSelect from '../../widgets/selectors/LanguageSelect.tsx'
import useIUnderstandLanguages from './hooks/useIUnderstandLanguages.ts'
import InputText from '../../widgets/inputs/InputText.tsx'
import Textarea from '../../widgets/textarea/Textarea.tsx'
import type {LangCode} from '../../../domain/LangCode.ts';
import type {Identifier} from '../../../domain/identity/Identifier.ts';
import type {Tag} from '../../../domain/Tag.ts';
import useDefaultLanguage from '../shared/hooks/useDefaultLanguage.ts';
import log from 'loglevel'
const logRender = log.getLogger('render')

type EntryAddProps = {
  add: (word: string,
        wordLang: LangCode,
        definition: string,
        definitionLang: LangCode,
        tagLabels: Identifier<Tag>[]) => Promise<void>
}

const EntryAdd = ({add}: EntryAddProps): JSX.Element => {
  logRender.debug('Rendering EntryAdd component')
  const {languages: learningLanguages, loading: lll} = useLearningLanguages()
  const {languages: understandLanguages, loading: lul} = useIUnderstandLanguages()
  const defaultLanguage = useDefaultLanguage()
  const [word, setWord] = useState<string>('')
  const [definition, setDescription] = useState<string>('')
  const [learningLanguage, setLearningLanguage] = useState(learningLanguages[0] || defaultLanguage)
  const [understandLanguage, setUnderstandLanguage] = useState(understandLanguages[0] || defaultLanguage)

  log.debug({
    learningLanguages, understandLanguages, learningLanguage, understandLanguage
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    log.debug('Submitting new entry:', {
      word,
      definition: definition,
      learningLanguage,
      understandLanguage,
    })
    add(word, learningLanguage.code, definition, understandLanguage.code, [])
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
      <div className="flex-1"/>
      <PrimaryButton type="submit" title='Add New Entry'/>
    </div>
    <br/>
    <div className="flex items-center space-x-4">
      <LanguageSelect
        languages={understandLanguages}
        defaultLanguage={learningLanguages[0]}
        loading={lul}
        onChange={(s) => setUnderstandLanguage(s)}/>
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
