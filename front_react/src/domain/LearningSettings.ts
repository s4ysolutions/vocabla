import type {Lang} from './Lang.ts';
import type {Identified} from './identity/Identified.ts';
import type {TagSmall} from './TagSmall.ts';

export type LearningSettings = {
  readonly learnLangs: ReadonlyArray<Lang>,
  readonly knownLangs: ReadonlyArray<Lang>,
  readonly tags: ReadonlyArray<Identified<TagSmall>>
}

export const LearningSettings = {
  empty: {
    learnLangs: [],
    knownLangs: [],
    tags: []
  } as LearningSettings
}
