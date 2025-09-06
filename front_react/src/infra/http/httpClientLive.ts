import {Effect, Layer} from 'effect';
import type {Method} from './Method.ts';
import type {HTTPError} from './errors/HTTPError.ts';
import {tt} from '../../translable/Translatable.ts';
import type {JsonDecodingError} from './errors/JsonDecodingError.ts';
import {HttpClientTag} from './HttpClient.ts';
import {clientError, type ClientError} from './errors/ClientError.ts';

const _handleOk = (response: Response): Effect.Effect<unknown, JsonDecodingError> =>
  Effect.tryPromise(() => response.json() as Promise<unknown>).pipe(
    Effect.map((body) =>
      body),
    Effect.mapError((error): JsonDecodingError => ({
      error,
      response
    } as JsonDecodingError))
  );

const _handleHttpError = (response: Response): Effect.Effect<never, HTTPError> =>
  Effect.tryPromise(() => response.text()).pipe(
    Effect.flatMap((errorBody) => Effect.fail({
      status: response.status,
      statusText: response.statusText,
      body: errorBody,
    } as HTTPError)),
    Effect.catchAll((bodyReadError) =>
      Effect.fail({
        status: response.status,
        statusText: response.statusText,
        bodyReadError
      } as HTTPError)
    )
  );

const _prettyPrintError = (err: unknown, indent = 0): string => {
  const prefix = '  '.repeat(indent);

  if (!(err instanceof Error)) {
    return prefix + String(err);
  }

  let result = `${prefix}${err.name}: ${err.message ?? String(err)}`;

  if (err instanceof AggregateError && Array.isArray(err.errors)) {
    for (const innerErr of err.errors) {
      result += '\n' + _prettyPrintError(innerErr, indent + 1);
    }
  }

  if ('cause' in err && err.cause instanceof Error) {
    result +=
      '\n' + prefix + 'Caused by:\n' + _prettyPrintError(err.cause, indent + 1);
  }

  return result;
};

const _httpRequestWithFetch = (
  method: Method,
  url: string,
  body?: unknown
): Effect.Effect<unknown, HTTPError | ClientError | JsonDecodingError> =>
  Effect.tryPromise(() =>
    fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
        Authorization: 'Bearer ' + 1,
      },
      body: body ? JSON.stringify(body) : null,
    })
  ).pipe(
    Effect.mapError((error): ClientError => {
        if (error._tag === 'UnknownException' && 'cause' in error) {
          const cause0 = error.cause;
          if (cause0 instanceof TypeError && 'cause' in cause0) {
            const cause = cause0.cause;
            if (cause instanceof AggregateError) {
              let message = '';
              for (const innerErr of cause.errors) {
                if ('message' in innerErr) {
                  message += innerErr.message + ';\n';
                }
              }
              if (message) {
                return clientError(tt`Failed to call fetch:\n ${message}`, error);
              }
            }
          }
        }
        return clientError(tt`Failed to call fetch`, error)
      }
    ),
    Effect.flatMap((response): Effect.Effect<unknown, HTTPError | JsonDecodingError> => {
      if (response.ok) {
        return _handleOk(response)
      } else {
        return _handleHttpError(response)
      }
    }),
    Effect
      .tapError((error) =>
        Effect.logError('httpRequestWithFetch failed: ' + _prettyPrintError(error))
      ));


/**
 * Live implementation of HttpClient using Fetch API
 */

const httpClientLayer = Layer.succeed(
  HttpClientTag,
  HttpClientTag.of({
    execute: _httpRequestWithFetch
  })
)

export default httpClientLayer
