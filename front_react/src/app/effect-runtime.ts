import * as Effect from 'effect/Effect';
import * as ManagedRuntime from 'effect/ManagedRuntime';
import {type UseCases, vocablaAppLive} from './VocablaApp.ts';

const appRuntime: ManagedRuntime.ManagedRuntime<UseCases, never> = ManagedRuntime.make(vocablaAppLive);

// Universal function to run ANY effect with all app layers provided
export const promiseAppEffect = <A, E, R>(
  effect: Effect.Effect<A, E, R & UseCases>
): Promise<A> =>
  appRuntime.runPromise(effect)// as Effect.Effect<A, E, never>);

// For effects that need custom error handling
export const promiseAppEffectExit = <A, E, R>(
  effect: Effect.Effect<A, E, R & UseCases>
) =>
  appRuntime.runPromiseExit(effect as Effect.Effect<A, E, never>);
