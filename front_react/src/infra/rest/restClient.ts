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

export type Get<DTO, OUT> = {
  url: string;
  decoder: (input: DTO) => Effect.Effect<OUT, ParseError>;
}

export interface RestClient {
  post: <IN, OUT>(args: Post<IN, OUT>) => Effect.Effect<OUT, ClientError | HTTPError | JsonDecodingError | ParseError>;
  get: <DTO, OUT>(args: Get<DTO, OUT>) => Effect.Effect<OUT, ClientError | HTTPError | JsonDecodingError | ParseError>;
}

export class RestClientTag extends Context.Tag('RestTag')<
  RestClientTag,
  RestClient
>() {
}
