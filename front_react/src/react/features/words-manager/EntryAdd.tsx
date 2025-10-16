import React, {type JSX, useCallback, useEffect, useState} from 'react'
import PrimaryButton from '../../widgets/buttons/PrimaryButton.tsx'
import LanguageSelect from '../../widgets/selectors/LanguageSelect.tsx'
import InputText from '../../widgets/inputs/InputText.tsx'
import Textarea from '../../widgets/textarea/Textarea.tsx'
import {emptyLangCode, isEmptyLangCode, type LangCode} from '../../../domain/LangCode.ts';
import type {Identifier} from '../../../domain/identity/Identifier.ts';
import type {Tag} from '../../../domain/Tag.ts';
import log from 'loglevel'
import useLearnLanguages from './hooks/useLearnLanguages.ts';
import useKnownLanguages from './hooks/useKnownLanguages.ts';
import useDefaultLanguage from '../shared/hooks/useDefaultLanguage.ts';
import useUnknownLanguage from '../shared/hooks/useUnknownLanguage.ts';

const logRender = log.getLogger('render')

type EntryAddProps = {
  add: (word: string,
        wordLang: LangCode,
        definition: string,
        definitionLang: LangCode,
        tagIds: Identifier<Tag>[],
  ) => Promise<void>,
}

const EntryAdd = ({add}: EntryAddProps): JSX.Element => {
  const defaultLanguage = useDefaultLanguage()
  const unknownLangauge = useUnknownLanguage()
  const {learnLanguages, loading: lll} = useLearnLanguages()
  const {knownLanguages, loading: lkl} = useKnownLanguages()

  const learnLangInit = learnLanguages.length > 0 ? learnLanguages[0]!.code : unknownLangauge?.code || emptyLangCode
  const knownLangInit = knownLanguages.length > 0 ? knownLanguages[0]!.code : defaultLanguage?.code || emptyLangCode

  const [learnSelected, setLearnSelected] = useState(learnLangInit)
  const [knownSelected, setKnownSelected] = useState(knownLangInit)

  const [word, setWord] = useState<string>('')
  const [definition, setDescription] = useState<string>('')

  logRender.debug('Rendering EntryAdd component', {learnSelected, lll, learnLangInit, knownSelected, word, definition})

  useEffect(() => {
    log.debug('Learn languages or default language changed, updating selected learning language.', {learnLangInit})
    setLearnSelected(learnLangInit)
  }, [learnLangInit]);

  useEffect(() => {
    setKnownSelected(knownLangInit)
  }, [knownLangInit]);

  const handleSubmit = useCallback((e: React.FormEvent) => {
    e.preventDefault()
    log.debug('Submitting new entry:', {
      word,
      definition: definition,
      learningLanguage: learnSelected,
      knownLanguage: knownSelected,
    })
    if (!word || !definition) {
      log.warn('Word or definition is empty, aborting submission.')
      return
    }
    if (!learnSelected) {
      log.warn('No learning language selected, aborting submission.')
      return
    }
    if (!knownSelected) {
      log.warn('No known language selected, aborting submission.')
      return
    }

    add(word, learnSelected, definition, knownSelected, [])
    // Add logic to handle form submission
  }, [add, word, definition, learnSelected, knownSelected])

  return <form onSubmit={handleSubmit}>
    <div className="flex items-center space-x-4">
      <LanguageSelect
        languages={learnLanguages}
        selectedCode={learnSelected}
        loading={lll || isEmptyLangCode(learnLangInit)}
        onChange={(lang) => setLearnSelected(lang.code)}
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
        languages={knownLanguages}
        //fallbackLanguage={defaultLanguage || knownLanguages[0]!}
        selectedCode={knownSelected}
        loading={lkl || isEmptyLangCode(knownLangInit)}
        onChange={(lang) => setKnownSelected(lang.code)}/>
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
