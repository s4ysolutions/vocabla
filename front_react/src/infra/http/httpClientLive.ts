import {Effect, Layer} from 'effect';
import HttpClient from './HttpClient.ts';
import type {Method} from './Method.ts';
import type {HTTPError} from './HTTPError.ts';
import {infraError, type InfraError} from '../../app-repo/infraError.ts';
import {tt} from '../../translable/Translatable.ts';
import type {JsonDecodingError} from './JsonDecodingError.ts';

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
): Effect.Effect<unknown, HTTPError | InfraError | JsonDecodingError> =>
  Effect.tryPromise(() =>
    fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      body: body ? JSON.stringify(body) : null,
    })
  ).pipe(
    Effect.mapError((error): InfraError =>
      infraError(tt`Failed to call fetch`, error)
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

const httpClientLive = Layer.succeed(
  HttpClient,
  HttpClient.of({
    execute: _httpRequestWithFetch
  })
)

export default httpClientLive
