import Definition from "./definition"
import Lang from "../../../../domain/Lang.ts"

interface Entry {
    readonly word: string
    readonly definitions: Array<Definition>
    readonly lang: Lang
}

export default Entry
