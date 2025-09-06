import type {Translatable} from '../../../translable/Translatable.ts';

export type ClientError = {
  message: Translatable;
  cause?: unknown;
  _tag?: 'ClientError';
};

export const clientError = (message: Translatable, cause?: unknown): ClientError => ({
  message,
  cause
});
