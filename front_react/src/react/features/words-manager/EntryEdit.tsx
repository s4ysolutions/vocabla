import React, {type ReactElement, useEffect, useMemo, useState} from 'react'
import PrimaryButton from '../../widgets/buttons/PrimaryButton.tsx'
import InputText from '../../widgets/inputs/InputText.tsx'
import CancelButton from '../../widgets/buttons/CancelButton copy.tsx'
import LanguageSelect from '../../widgets/selectors/LanguageSelect.tsx'
import DefinitionEdit from './DefinitionEdit.tsx'
import type {Entry} from '../../../domain/Entry.ts';
import {emptyLangCode, type LangCode} from '../../../domain/LangCode.ts';
import {hashString} from '../../../domain/hash/hashString.ts';
import useLearnLanguages from './hooks/useLearnLanguages.ts';
import useKnownLanguages from './hooks/useKnownLanguages.ts';
import useUnknownLanguage from '../shared/hooks/useUnknownLanguage.ts';
import loglevel from 'loglevel';

const log = loglevel.getLogger('render')

interface DefintionHolder {
  readonly id: number
  language: LangCode
  s: string
}

const createDefinitionHolder = (definitionLangCode: LangCode, s: string): DefintionHolder => ({
  id: s.trim() == '' ? new Date().getTime() : hashString(definitionLangCode + '|' + s),
  language: definitionLangCode,
  s,
})

// make sure there's always one empty definition holder at the end
const addEmtptyDefinitionHolder = (
  knownLanguageCode: LangCode,
  definitionHolders: DefintionHolder[],
  changedDefId: number | null): DefintionHolder[] => {

  if (definitionHolders.length === 0) {
    return [createDefinitionHolder(knownLanguageCode, '')]
  }

  const hole = definitionHolders.findIndex((d) => d.s.trim() === '')

  // empty hole is already at the end
  if (hole === definitionHolders.length - 1)
    return definitionHolders

  // there's no empty hole, add one
  if (hole === -1)
    return definitionHolders.concat(createDefinitionHolder(knownLanguageCode, ''))

  const noEmpties = definitionHolders.filter((d) => (d.s.trim() !== '' || d.id == changedDefId))

  return noEmpties.filter(d => d.s.trim() === '').length === 0
    ? noEmpties.concat(createDefinitionHolder(knownLanguageCode, ''))
    : noEmpties
}

interface Props {
  entry: Entry
  onComplete: () => void
}

const EntryEdit: React.FC<Props> = ({entry, onComplete}): ReactElement => {

  const [word, setWord] = useState<string>(entry.word.s)
  const [wordLangCode, setWordLangCode] = useState(entry.word.langCode)

  const {learnLanguages, loading: lll} = useLearnLanguages()
  const {knownLanguages, loading: lkl} = useKnownLanguages()
  const unknownLanguage = useUnknownLanguage()
  const knownLanguageCode = useMemo(() =>
      knownLanguages.length > 0
        ? knownLanguages[0]!.code
        : unknownLanguage?.code || emptyLangCode,
    [knownLanguages, unknownLanguage])

  const initDefinitions = useMemo(() =>
      lkl
        ? []
        : addEmtptyDefinitionHolder(
          knownLanguageCode,
          entry.definitions
            .map((d) => createDefinitionHolder(d.localized.langCode, d.localized.s))
          , null)
    , [entry, lkl, knownLanguageCode])

  const [definitions, setDefinitions] = useState<DefintionHolder[]>(initDefinitions)

  log.debug('Rendering EntryEdit component', {entry, definitions, lkl})

  useEffect(() => {
    setDefinitions(initDefinitions)
  }, [initDefinitions])

  const handleSave = () => {
    console.log({
      word,
      //learningLanguage: learnLanguage,
      // definition: definition,
      // learningLanguage,
      // understandLanguage,
    })
    onComplete()
  }

  return (
    <div className="space-y-4" id="entry-edit">
      {/* Language Selector and Word Input */}
      <div className="flex items-center space-x-4" id="word-input">
        <LanguageSelect
          languages={learnLanguages}
          selectedCode={wordLangCode}
          //defaultLanguage={wordLang}
          loading={lll}
          onChange={(langCode) => setWordLangCode(langCode.code)}
        />
        <InputText
          id="word"
          defaultValue={word}
          onChange={(e) => setWord(e.target.value)}
        />
      </div>

      <div className="overflow-y-auto  max-h-full" id="definition-edit">
        <label className="block text-sm font-medium text-gray-700">Definitions</label>
        <ul className="space-y-2">
          {lkl
            ? <p>Loading...</p>
            : definitions.map((def) =>
              <DefinitionEdit
                key={def.id}
                knownLanguages={knownLanguages}
                definition={def.s}
                onChangeDefinition={s => {
                  def.s = s;
                  setDefinitions(addEmtptyDefinitionHolder(knownLanguageCode, definitions, def.id))
                }}
                langCode={def.language}
                onChangeLanguage={(lang) => def.language = lang.code}
              />
            )}
        </ul>
      </div>

      {/* Action Buttons */}
      <div className="flex justify-end space-x-2" id="action-buttons">
        <CancelButton onClick={onComplete} title='Cancel'/>
        <PrimaryButton onClick={handleSave} title='Save'/>
      </div>
    </div>
  )
}

export default EntryEdit
