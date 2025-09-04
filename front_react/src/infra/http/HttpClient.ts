import {Context, Effect} from 'effect';
import type {HTTPError} from './HTTPError.ts';
import {type InfraError} from '../../app-repo/infraError.ts';
import type {Method} from './Method.ts';
import type {JsonDecodingError} from './JsonDecodingError.ts';

export class HttpClient extends Context.Tag('CounterNumber')<
  HttpClient,
  { execute: (method: Method, url: string, body?: unknown) => Effect.Effect<unknown, HTTPError | InfraError | JsonDecodingError> }
>() {
}
export default HttpClient
