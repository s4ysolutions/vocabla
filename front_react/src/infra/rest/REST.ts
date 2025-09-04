import {Context, Effect, Schema} from 'effect';
import type {InfraError} from '../../app-repo/infraError.ts';
import type {HTTPError} from '../http/HTTPError.ts';
import type {JsonDecodingError} from '../http/JsonDecodingError.ts';
import type {ParseError} from 'effect/ParseResult';

export class REST extends Context.Tag('REST')<
  REST,
  {
    post: <IN, OUT>(url: string, body: IN, schemaIn: Schema.Schema<IN>, schemaOut: Schema.Schema<OUT>) => Effect.Effect<OUT, InfraError | HTTPError | JsonDecodingError | ParseError>;
  }
>() {
}
