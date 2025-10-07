import type {Translatable} from '../../translable/Translatable.ts';

export type AppError = {
  message: Translatable
  _tag?: 'AppError'
}

export const AppError = (message: Translatable): AppError => ({
  message,
  _tag: 'AppError'
});
