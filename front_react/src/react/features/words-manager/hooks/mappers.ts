import {type AsyncData, LoadingData, matchAsyncData, SuccessData} from '../../../../app-ports/types.ts';
import type {LearningSettings} from '../../../../domain/LearningSettings.ts';
import type {Lang} from '../../../../domain/Lang.ts';
import type {AppError} from '../../../../app-ports/errors/AppError.ts';

// extract default language from LanguagesUseCases
// and create a function that maps array of lang codes to array of Langs
// handling the case when the array is empty by returning the default language

const als2langs = (
  als: AsyncData<LearningSettings, AppError>,
  f: (ls: LearningSettings) => ReadonlyArray<Lang>): AsyncData<ReadonlyArray<Lang>> =>
  matchAsyncData(als,
    (previous) => LoadingData(previous ? f(previous) : undefined),
    () => SuccessData([]),
    (ls) => SuccessData(f(ls))
  );

export const learningSettings2learnLangs =
  (fallbackLang: Lang) =>
    (als: AsyncData<LearningSettings, AppError>): AsyncData<ReadonlyArray<Lang>> =>
      als2langs(als, ls => ls.learnLangs.length > 0 ? ls.learnLangs : [fallbackLang]);

export const learningSettings2knownLangs =
  (fallbackLang: Lang) =>
    (als: AsyncData<LearningSettings, AppError>): AsyncData<ReadonlyArray<Lang>> =>
      als2langs(als, ls => ls.knownLangs.length > 0 ? ls.knownLangs : [fallbackLang]);
