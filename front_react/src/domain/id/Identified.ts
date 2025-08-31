import type {Identifier} from "./Identifier.ts";

export type Identified<E> = {
    readonly id: Identifier<E>,
    readonly e: E
}