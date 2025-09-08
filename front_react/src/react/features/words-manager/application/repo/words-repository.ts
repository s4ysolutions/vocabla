import { Context } from "effect";
import { Effect } from "effect/Effect";

export type DTOID = string;

interface DefinitionDTO {
  readonly definition: string;
  readonly language: string;
}

export interface EntryDTO {
  readonly id: DTOID;
  readonly word: string;
  readonly lang: string;
  readonly definitions: Array<DefinitionDTO>;
  readonly tags: Array<DTOID>;
}

/**
 * Creates a new entry in the dictionary.
 *
 * @param ownerID - The ID of the entry owner.
 * @param word - The word being defined.
 * @param wordLang - Language of the word.
 * @param definition - The definition text.
 * @param definitionLang - Language of the definition.
 * @param tagLabels - A list of tags for categorization.
 */

interface WordsRepository {
  addEntry(
    ownerID: DTOID,
    word: string,
    wordLang: string,
    definition: string,
    definitionLang: string,
    tagLabels: Array<string>
  ): Effect<DTOID, string, never>;
  entriesByOwner(ownerID: DTOID): Effect<Array<EntryDTO>, string, never>;
}

export class WordsRepositoryTag extends Context.Tag("WordsAdapter")<
  WordsRepositoryTag,
  WordsRepository
>() {}

export default WordsRepository;
