export type AsyncData<T, E = Error> =
  | { _state: 'loading' }
  | { _state: 'error'; error: E }
  | { _state: 'success'; data: T }

export const LoadingData = <T, E = Error>() => ({_state: 'loading'} as AsyncData<T, E>);
export const ErrorData = <T, E = Error>(error: E) => ({_state: 'error', error} as AsyncData<T, E>);
export const SuccessData = <T, E = Error>(data: T) => ({_state: 'success', data} as AsyncData<T, E>);
