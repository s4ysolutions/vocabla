import type {Translatable} from '../../translable/Translatable.ts';

export type AppError = {
  message: Translatable
  _tag?: 'AppError'
}

export const appError = (message: Translatable): AppError => ({
  message,
});
