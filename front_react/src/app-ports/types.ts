export type AsyncData<T> =
  | { _state: 'loading' }
  | { _state: 'error'; error: Error }
  | { _state: 'success'; data: T }

export const LoadingData = <T>() =>({ _state: 'loading' } as AsyncData<T>);
export const ErrorData = <T>(error: Error) => ({ _state: 'error', error } as AsyncData<T>);
export const SuccessData = <T>(data: T) => ({ _state: 'success', data } as AsyncData<T>);
