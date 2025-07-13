import Definition from "./definition"
import Lang from "../../../../domain/lang"

interface Entry {
    readonly word: string
    readonly definitions: Array<Definition>
    readonly lang: Lang
}

export default Entry