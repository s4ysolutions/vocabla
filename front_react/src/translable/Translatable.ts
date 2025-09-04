export type Translatable = {
  parts: TemplateStringsArray,
  values: string[]
}

export const tt = (parts: TemplateStringsArray, ...values: unknown[]): Translatable => ({
  parts,
  values
})
