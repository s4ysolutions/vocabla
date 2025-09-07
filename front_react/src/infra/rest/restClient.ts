import {Context, Effect} from 'effect';
import type {HTTPError} from '../http/errors/HTTPError.ts';
import type {JsonDecodingError} from '../http/errors/JsonDecodingError.ts';
import type {ClientError} from '../http/errors/ClientError.ts';
import type {ParseError} from 'effect/ParseResult';

export type Post<IN, OUT> = {
  url: string;
  body: IN;
  decoder: (input: unknown) => Effect.Effect<OUT, ParseError>;
  //schemaOut: Schema.Schema<OUT, Odto>;
}

export type Get<OUT> = {
  url: string;
  decoder: (input: unknown) => Effect.Effect<OUT, ParseError>;
  //schemaOut: Schema.Schema<OUT, Odto>;
}

export interface RestClient {
  post: <IN, OUT>(args: Post<IN, OUT>) => Effect.Effect<OUT, ClientError | HTTPError | JsonDecodingError | ParseError>;
  get: <OUT>(args: Get<OUT>) => Effect.Effect<OUT, ClientError | HTTPError | JsonDecodingError | ParseError>;
}

export class RestClientTag extends Context.Tag('RestTag')<
  RestClientTag,
  RestClient
>() {
}
