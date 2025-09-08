import * as Effect from "effect/Effect";
import { Id, id } from "../../../../../domain/id/Id.ts";
import { WordsRepositoryTag } from "../repo/words-repository.ts";

const addEntryUseCase = (
  ownerId: Id,
  word: string,
  wordLang: string,
  definition: string,
  definitionLang: string,
  tagLabels: string[]
): Effect.Effect<Id, string, WordsRepositoryTag> =>
  Effect.flatMap(WordsRepositoryTag, (repo) =>
    repo
      .addEntry(ownerId, word, wordLang, definition, definitionLang, tagLabels)
      .pipe(Effect.map((dtoid) => id(dtoid)))
  );

export default addEntryUseCase;
