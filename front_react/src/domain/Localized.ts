import type {Lang} from "./Lang.ts";

export type Localized = {
    readonly lang: Lang
    readonly s: string
}
