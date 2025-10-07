import React, {type ReactElement} from 'react'
import Textarea from '../../widgets/textarea/Textarea.tsx'
import LanguageSelect from '../../widgets/selectors/LanguageSelect.tsx'
import type {Lang} from '../../../domain/Lang.ts';
import type {LangCode} from '../../../domain/LangCode.ts';

interface Props {
  knownLanguages: ReadonlyArray<Lang>,
  definition: string
  langCode: LangCode
  onChangeDefinition: (string: string) => void
  onChangeLanguage: (lang: Lang) => void
}

const DefinitionEdit: React.FC<Props> = ({
                                           definition, onChangeDefinition, langCode, onChangeLanguage, knownLanguages
                                         }): ReactElement => <div className="flex item-start space-y-4">
  {/* Language Selector */}
  <LanguageSelect
    languages={knownLanguages}
    selectedCode={langCode}
    onChange={onChangeLanguage}
  />
  {/* Textarea for Definition */}
  <Textarea
    defaultValue={definition}
    onChange={(e) => onChangeDefinition(e.target.value)}
    rows={3}
  />
</div>

export default DefinitionEdit
