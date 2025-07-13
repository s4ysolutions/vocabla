import * as Effect from "effect/Effect";
import WordsRepository, {
  DTOID,
  EntryDTO,
} from "../features/words-manager/application/repo/words-repository";
import { get, post } from "./fetch-effect";


const rest: WordsRepository = {
  addEntry: (
    ownerId,
    word,
    wordLang,
    definition,
    definitionLang,
    tagLabels
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
      json => json.entries
    ),
};

export default rest;
