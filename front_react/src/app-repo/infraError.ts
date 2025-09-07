import type {Translatable} from '../translable/Translatable.ts';

export type InfraError = {
  message: Translatable;
  cause?: unknown;
  _tag?: 'InfraError';
};

export const infraError = (message: Translatable, cause?: unknown): InfraError => ({
  message,
  cause,
});
