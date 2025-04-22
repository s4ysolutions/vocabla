export type Id = string & { __brand: 'Id' }

export const id = (id: string): Id => id as Id
