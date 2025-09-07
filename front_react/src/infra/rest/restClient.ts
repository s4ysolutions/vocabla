import {Context, Effect, Schema} from 'effect';
import type {HTTPError} from '../http/errors/HTTPError.ts';
import type {JsonDecodingError} from '../http/errors/JsonDecodingError.ts';
import type {ClientError} from '../http/errors/ClientError.ts';
import type {ParseError} from 'effect/ParseResult';

export type Post<IN, OUT, Odto> = {
  url: string;
  body: IN;
  schemaOut: Schema.Schema<OUT, Odto>;
}

export type Get<OUT, Odto> = {
  url: string;
  schemaOut: Schema.Schema<OUT, Odto>;
}

export interface RestClient {
  post: <IN, OUT, Odto>(args: Post<IN, OUT, Odto>) => Effect.Effect<OUT, ClientError | HTTPError | JsonDecodingError | ParseError>;
  get: <OUT, Odto>(args: {
    url: string;
    schemaOut: Schema.Schema<OUT, Odto>;
  }) => Effect.Effect<OUT, ClientError | HTTPError | JsonDecodingError | ParseError>;
}

export class RestClientTag extends Context.Tag('RestTag')<
  RestClientTag,
  RestClient
>() {
}
