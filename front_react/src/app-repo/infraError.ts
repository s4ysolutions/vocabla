import type {Translatable} from '../translable/Translatable.ts';

export type InfraError = {
  message: Translatable;
  cause?: unknown;
};

export const infraError = (message: Translatable, cause?: unknown): InfraError => ({
  message,
  cause
});
