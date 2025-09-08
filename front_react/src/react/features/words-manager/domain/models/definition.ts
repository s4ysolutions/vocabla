import Lang from "../../../../../domain/Lang.ts";
import Localized from "./localized.ts";
import Source from "./source.ts";

interface Definition {
   readonly localized: Localized
   readonly source: Source
}

export const emptyDefinition = (lang: Lang) => ({
   localized: {
      lang: lang,
      s: "",
   },
   source: {
      title: "",
      url: undefined,
   },
})

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const isDefinition = (obj: any): obj is Definition => {
   return (
      obj &&
      typeof obj.localized === "object" &&
      typeof obj.source === "object" &&
      typeof obj.source.title === "string" &&
      (obj.source.url === undefined || typeof obj.source.url === "string")
   )
}

export default Definition
