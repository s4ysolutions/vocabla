import type {Owned} from "./mixins/Owned.ts";
import type {Student} from "./Student.ts";

export type Tag = {
    label: string
} & Owned<Student>;
