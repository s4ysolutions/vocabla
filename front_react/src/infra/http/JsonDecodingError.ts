export type JsonDecodingError = {
  __brand: 'JsonDecodingError';
  error?: unknown;
  response: Response
}
