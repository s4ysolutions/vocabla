import type {Tag} from './Tag.ts';
import type {Lang} from './Lang.ts';

export type LearningSettings = {
  readonly learnLangs: ReadonlyArray<Lang>,
  readonly knownLangs: ReadonlyArray<Lang>,
  readonly tags: ReadonlyArray<Tag>
}
