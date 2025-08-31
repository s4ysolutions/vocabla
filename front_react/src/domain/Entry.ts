import type {Localized} from "./Localized.ts";
import type {Owned} from "./mixins/Owned.ts";
import type { Student } from "./Student.ts";

export type Entry = {
  readonly word: Localized
  readonly definitions: Array<Entry.Definition>
} & Owned<Student>

export namespace Entry {
  export type Definition = {
    readonly localized: Localized
    readonly source: Source
  }
  export type Source = {
    readonly title: string,
    readonly url?: string,
  }
}

export const isEntryDefinition = (obj: unknown): obj is Entry.Definition =>
  obj != null &&
  typeof obj === "object" &&
  "localized" in obj &&
  "source" in obj &&
  typeof obj.localized === "object" &&
  typeof obj.source === "object" &&
  obj.source !== null &&
  "title" in obj.source &&
  typeof obj.source.title === "string" &&
  ("url" in obj.source ? typeof obj.source.url === "string" || obj.source.url === undefined : true)
