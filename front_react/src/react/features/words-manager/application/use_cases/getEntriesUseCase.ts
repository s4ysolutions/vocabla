import * as Effect from "effect/Effect";
import { Id } from "../../../../../domain/id/Id.ts";
import { WordsRepositoryTag, EntryDTO } from "../repo/words-repository.ts";
import Entry from "../../domain/models/entry.ts";
import Definition from "../../domain/models/definition.ts";

// Helper function to convert EntryDTO to Entry (moved from UI layer)
const convertEntryDTOToEntry = (entryDTO: EntryDTO): Entry => ({
  word: entryDTO.word,
  lang: { code: entryDTO.lang, name: entryDTO.lang }, // Simple conversion
  definitions: entryDTO.definitions.map(def => ({
    localized: {
      lang: { code: def.language, name: def.language },
      s: def.definition
    },
    source: { title: "Manual" }
  } as Definition))
});

const getEntriesUseCase = (
  ownerId: Id
): Effect.Effect<Entry[], string, WordsRepositoryTag> =>
  Effect.flatMap(WordsRepositoryTag, (repo) =>
    repo.entriesByOwner(ownerId).pipe(
      Effect.map(entryDTOs => entryDTOs.map(convertEntryDTOToEntry))
    )
  );

export default getEntriesUseCase;
