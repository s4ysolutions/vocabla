import React, {useEffect} from 'react';

import T from '../../../l10n';
import LanguagesSelector from './LanguagesSelector.tsx';
import TagsManager from './TagsManager.tsx';
import useLearningSettings from './hooks/useLearningSettings.ts';
import type {LangCode} from '../../../domain/LangCode.ts';
import loglevel from 'loglevel';
import {isLoading, matchAsyncData} from '../../../app-ports/types.ts';
import {LearningSettings as LearningSettingsType} from '../../../domain/LearningSettings.ts';
import ProgressInfinity from '../../widgets/progress-infinity';
import type {Lang} from '../../../domain/Lang.ts';

const renderLog = loglevel.getLogger('render')

const codesSet = (langs: ReadonlyArray<Lang>) => new Set(langs.map(l => l.code));

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
      removeKnownLang,
      addTag,
      removeTag
    } = useLearningSettings()

    renderLog.debug('Render LearningSettings component', {learningSettings})

    const learningLangs =
      matchAsyncData(learningSettings,
        (previous) => previous ? codesSet(previous.learnLangs) : new Set<LangCode>(),
        () => new Set<LangCode>(),
        (data) => codesSet(data.learnLangs)
      );

    const knownLangs = matchAsyncData(learningSettings,
      (previous) => previous ? codesSet(previous.knownLangs) : new Set<LangCode>(),
      () => new Set<LangCode>(),
      (data) => new Set(data.knownLangs.map(l => l.code)))

    const tags = matchAsyncData(learningSettings,
      (previous) => previous?.tags ? previous.tags : [],
      () => [],
      (data: LearningSettingsType) => data.tags)

    return (
      <div className="h-full w-full flex flex-col overflow-auto sm:overflow-hidden relative">
        {(isLoading(learningSettings)) && (
          <div className="absolute inset-0 bg-white/50 flex items-center justify-center z-10">
            <ProgressInfinity/>
          </div>
        )}
        {/* Top Section */}
        <div className="flex sm:flex-shrink-0 sm:flex-grow-0 flex-col p-2 sm:overflow-y-hidden">
          <TagsManager tags={tags} addTag={addTag} removeTag={removeTag}/>
        </div>
        {/* Bottom Section */}
        <div className='flex sm:flex-grow flex-col sm:flex-row sm:overflow-y-hidden'>
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

      </div>
    );
  }
;

export default LearningSettings;
