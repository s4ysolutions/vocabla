export type LangCode = string & { __brand: 'LangCode' }

export type Lang = {
    readonly code: LangCode
    readonly name: string
    readonly flag?: string
}
