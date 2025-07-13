interface Source {
    readonly title: string,
    readonly url?: string,
}

export const defaultSource: Source = {
    title: '',
    url: undefined,
}

export default Source