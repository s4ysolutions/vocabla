
declare const IdBrand: unique symbol;
export type Id = number & { [IdBrand]?: void }

export const id = (id: number): Id => id as Id
