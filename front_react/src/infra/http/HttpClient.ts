import {Context, Effect} from 'effect';
import type {HTTPError} from './errors/HTTPError.ts';
import type {Method} from './Method.ts';
import type {JsonDecodingError} from './errors/JsonDecodingError.ts';
import type {ClientError} from './errors/ClientError.ts';

export interface HttpClient {
  execute: (method: Method, url: string, body?: unknown) => Effect.Effect<unknown, HTTPError | ClientError | JsonDecodingError>
}

export class HttpClientTag extends Context.Tag('HttpClientTag')<
  HttpClientTag,
  HttpClient
>() {
}
