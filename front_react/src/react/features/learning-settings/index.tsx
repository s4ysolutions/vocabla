import React, {useEffect} from 'react';

import T from '../../../l10n';
import LanguagesSelector from './LanguagesSelector.tsx';
import TagsManager from './TagsManager.tsx';
import useLearningSettings from './hooks/useLearningSettings.ts';
import type {LangCode} from '../../../domain/LangCode.ts';
import loglevel from 'loglevel';

const renderLog = loglevel.getLogger('render')

const LearningSettings: React.FC = () => {
    useEffect(() => {
      renderLog.debug('Mount LearningSettings component')
      return () => {
        renderLog.debug('Umount LearningSettings component')
      }
    }, []);

    const {
      learningSettings,
      addLearnLang,
      removeLearnLang,
      addKnownLang,
      removeKnownLang
    } = useLearningSettings()

    const learningLangs = learningSettings._state === 'success' ? new Set(learningSettings.data.learnLangs.map(l => l.code)) : new Set<LangCode>()
    const knownLangs = learningSettings._state === 'success' ? new Set(learningSettings.data.knownLangs.map(l => l.code)) : new Set<LangCode>()

    return (
      <div className="h-full w-full flex flex-col overflow-auto sm:overflow-hidden">
        {/* Top Section */}

        <div className='flex sm:flex-1 flex-col sm:flex-row sm:overflow-y-hidden'>
          <div className='h-full flex flex-col w-full sm:w-1/2 p-2'>
            <h2 className="text-lg font-bold mb-2">{T`Languages I want to learn`}</h2>
            <div className="sm:overflow-y-auto flex-1">
              <LanguagesSelector
                selected={learningLangs}
                addLang={addLearnLang}
                removeLang={removeLearnLang}
              />
            </div>
          </div>
          <div className='h-full flex flex-col w-full sm:w-1/2 p-2'>
            <h2 className="text-lg font-bold mb-2">{T`Languages I understand`}</h2>
            <div className="sm:overflow-y-auto flex-1">
              <LanguagesSelector
                selected={knownLangs}
                addLang={addKnownLang}
                removeLang={removeKnownLang}
              />
            </div>
          </div>
        </div>

        {/* Bottom Section */}
        <div className="flex-1 flex flex-col p-2 sm:overflow-y-hidden">
          <TagsManager/>
        </div>
      </div>
    );
  }
;

export default LearningSettings;
