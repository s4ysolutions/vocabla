import * as Effect from "effect/Effect";
import WordsRepository, {
  DTOID,
  EntryDTO,
  WordsRepositoryTag,
} from "../features/words-manager/application/repo/words-repository";
import { get, post } from "./fetch-effect";
import { Layer } from "effect";

const wordsAdapterRest: WordsRepository = {
  addEntry: (
    ownerId: DTOID,
    word: string,
    wordLang: string,
    definition: string,
    definitionLang: string,
    tagLabels: Array<string>
  ): Effect.Effect<DTOID, string, never> =>
    post<
      {
        ownerId: DTOID;
        word: string;
        wordLang: string;
        definition: string;
        definitionLang: string;
        tagLabels: Array<string>;
      },
      { entryId: string },
      string
    >(
      "/words/entries",
      {
        ownerId,
        word,
        wordLang,
        definition,
        definitionLang,
        tagLabels,
      },
      (json) => String(json.entryId)
    ),
  entriesByOwner: (
    ownerID: DTOID
  ): Effect.Effect<Array<EntryDTO>, string, never> =>
    get<{ entries: Array<EntryDTO> }, Array<EntryDTO>>(
      `/words/entries/${ownerID}`,
      (json) => json.entries
    ),
};

export const WordsAdapterRestLayer = Layer.succeed(
  WordsRepositoryTag,
  WordsRepositoryTag.of(wordsAdapterRest)
);

export default wordsAdapterRest;
