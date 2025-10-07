import loglevel from 'loglevel';

const log = loglevel.getLogger('AsyncData');
log.setLevel(loglevel.levels.INFO);

export type AsyncData<T, E = Error> =
  | { _state: 'loading', previous?: T }
  | { _state: 'error'; error: E }
  | { _state: 'success'; data: T }

export const LoadingData = <T, E = Error>(previous?: T) => ({_state: 'loading', previous} as AsyncData<T, E>);
export const ErrorData = <T, E = Error>(error: E) => ({_state: 'error', error} as AsyncData<T, E>);
export const SuccessData = <T, E = Error>(data: T) => ({_state: 'success', data} as AsyncData<T, E>);

export const isLoading = <T, E>(data: AsyncData<T, E>): data is {
  _state: 'loading',
  previous?: T
} => data._state === 'loading';
export const isError = <T, E>(data: AsyncData<T, E>): data is { _state: 'error'; error: E } => data._state === 'error';
export const isSuccess = <T, E>(data: AsyncData<T, E>): data is {
  _state: 'success';
  data: T
} => data._state === 'success';

export const matchAsyncData = <T, E, R = void>(data: AsyncData<T, E>,
                                               loading: (previous?: T) => R,
                                               error: (error: E) => R,
                                               success: (data: T) => R,
): R => {
  switch (data._state) {
    case 'loading':
      log.debug('matchAsyncData: loading', data.previous);
      return loading(data.previous);
    case 'error':
      log.debug('matchAsyncData: error', data.error);
      return error(data.error);
    case 'success':
      log.debug('matchAsyncData: success', data.data);
      return success(data.data);
    default: {
      log.error('matchAsyncData: Unhandled case', data);
      // noinspection UnnecessaryLocalVariableJS
      const _exhaustiveCheck: never = data;
      throw new Error(`Unhandled case: ${JSON.stringify(_exhaustiveCheck)}`);
    }
  }
}
