import type {Identifier} from "../id/Identifier.ts";

export type Owned<E> = {
    readonly ownerId: Identifier<E>
}