import React, { ReactElement } from 'react'
import Lang from '../../../domain/Lang.ts'
import Textarea from '../../../react/widgets/textarea/Textarea.tsx'
import LanguageSelect from '../../../react/widgets/selectors/LanguageSelect.tsx'

interface Props {
  languagesIUnderstand: Lang[],
  defaultDefinition: string
  defaultLanguage: Lang
  onChangeDefinition: (string: string) => void
  onChangeLanguage: (lang: Lang) => void
}

const DefinitionEdit: React.FC<Props> = ({
  defaultDefinition, onChangeDefinition, defaultLanguage, onChangeLanguage, languagesIUnderstand
}): ReactElement => <div className="flex item-start space-y-4">
    {/* Language Selector */}
    <LanguageSelect
      languages={languagesIUnderstand}
      defaultLanguage={defaultLanguage}
      onChange={onChangeLanguage}
    />
    {/* Textarea for Definition */}
    <Textarea
      defaultValue={defaultDefinition}
      onChange={(e) => onChangeDefinition(e.target.value)}
      rows={3}
    />
  </div>

export default DefinitionEdit