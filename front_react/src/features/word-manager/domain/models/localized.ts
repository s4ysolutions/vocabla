import Lang from "../../../../domain/lang";

interface Localized {
    readonly lang: Lang
    readonly s: string
}


export default Localized