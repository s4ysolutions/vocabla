import Lang from "../../../../domain/Lang.ts";

interface Localized {
    readonly lang: Lang
    readonly s: string
}


export default Localized