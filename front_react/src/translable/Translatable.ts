export type Translatable = {
  parts: TemplateStringsArray,
  values: unknown[]
}

export const tt = (parts: TemplateStringsArray, ...values: unknown[]): Translatable => ({
  parts,
  values
})
