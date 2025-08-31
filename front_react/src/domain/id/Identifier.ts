// generic identifier with a phantom brand
declare const IdentifierBrand: unique symbol;
export type Identifier<E> = {
    readonly id: number;
} & {readonly [IdentifierBrand]?: E}
