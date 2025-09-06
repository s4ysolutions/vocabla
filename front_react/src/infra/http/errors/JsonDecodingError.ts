import type {ParseError} from 'effect/ParseResult';

export type JsonDecodingError = {
  error?: unknown;
  response?: Response;
  parseError?: ParseError;
  _tag?: 'JsonDecodingError';
}
