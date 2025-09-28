/**
 * Defines a REST client interface with methods for making POST and GET requests.
 */
import {Context, Effect} from 'effect';
import type {HTTPError} from '../http/errors/HTTPError.ts';
import type {JsonDecodingError} from '../http/errors/JsonDecodingError.ts';
import type {ClientError} from '../http/errors/ClientError.ts';
import type {ParseError} from 'effect/ParseResult';

export type Post<REQ, RESP, OUT> = {
  url: string;
  body: REQ;
  decoder: (input: RESP) => Effect.Effect<OUT, ParseError>;
  //schemaOut: Schema.Schema<OUT, Odto>;
}

export type Get<RESP, OUT> = {
  url: string;
  decoder: (input: RESP) => Effect.Effect<OUT, ParseError>;
}

export interface RestClient {
  post: <REQ, RESP, OUT>(args: Post<REQ, RESP, OUT>) => Effect.Effect<OUT, ClientError | HTTPError | JsonDecodingError | ParseError>;
  get: <RESP, OUT>(args: Get<RESP, OUT>) => Effect.Effect<OUT, ClientError | HTTPError | JsonDecodingError | ParseError>;
  delete: <RESP, OUT>(args: Get<RESP, OUT>) => Effect.Effect<OUT, ClientError | HTTPError | JsonDecodingError | ParseError>;
}

export class RestClientTag extends Context.Tag('RestTag')<
  RestClientTag,
  RestClient
>() {
}
