import * as Effect from 'effect/Effect';
import * as ManagedRuntime from 'effect/ManagedRuntime';
import {Fiber, Layer} from 'effect';
import type {LanguagesUseCasesTag} from '../app-ports/LanguagesUseCases.ts';
import type {LearningSettingsUseCasesTag} from '../app-ports/LearningSettingsUseCases.ts';
import LanguagesUseCasesLive from './LanguagesUseCasesLive.ts';
import MeUseCasesLive from './MeUseCasesLive.ts';
import RepositoryRestLive from '../infra/repo/RepositoryRestLive.ts';
import RestClientLive from '../infra/rest/RestClientLive.ts';
import HttpClientLive from '../infra/http/HttpClientLive.ts';
import LearningSettingsUseCasesLive from './LearningSettingsUseCasesLive.ts';
import type {MeUseCasesTag} from '../app-ports/MeUseCases.ts';
import type {EntriesUseCasesTag} from '../app-ports/EntriesUseCases.ts';
import EntriesUseCasesLive from './EntriesUseCasesLive.ts';

import loglevel from 'loglevel';

const log = loglevel.getLogger('effect-runtime')
log.setLevel(loglevel.levels.INFO)

const te = <A, E, R>(effect: Effect.Effect<A, E, R & UseCases>): Effect.Effect<A, E, R & UseCases> => {
  if (!effect)
    log.error('Running undefined effect')
  log.trace('Running effect', effect)
  return effect
}

const tf = <A, E>(fiber: Fiber.Fiber<A, E>): Fiber.Fiber<A, E> => {
  if (!fiber)
    log.error('Runnng undefined fiber')
  log.trace('Running fiber', fiber)
  return fiber
}


const repoLayer = Layer.provide(
  RepositoryRestLive.layer,
  Layer.provide(RestClientLive.layer, HttpClientLive.layer))
const languagesLayer = Layer.provide(
  LanguagesUseCasesLive.layer,
  repoLayer)

const appLayers = Layer.mergeAll(
  MeUseCasesLive.layer,
  languagesLayer,
  Layer.provide(
    LearningSettingsUseCasesLive.layer,
    Layer.mergeAll(repoLayer, MeUseCasesLive.layer, languagesLayer)),
  Layer.provide(
    EntriesUseCasesLive.layer,
    Layer.mergeAll(repoLayer, MeUseCasesLive.layer))
)

type UseCases = LanguagesUseCasesTag | LearningSettingsUseCasesTag | MeUseCasesTag | EntriesUseCasesTag

const appRuntime: ManagedRuntime.ManagedRuntime<UseCases, never> = ManagedRuntime.make(appLayers);

// Universal function to run ANY effect with all app layers provided
export const promiseAppEffect = <A, E>(
  effect: Effect.Effect<A, E, UseCases>
): Promise<A> =>
  appRuntime.runPromise(te(effect))

// For effects that need custom error handling
export const promiseAppEffectExit = <A, E, R>(
  effect: Effect.Effect<A, E, R & UseCases>
) =>
  appRuntime.runPromiseExit(te(effect));

export const forkAppEffect = <A, E, R>(
  effect: Effect.Effect<A, E, R & UseCases>
) => {
  const eLogged: Effect.Effect<A, E, R & UseCases> = te(effect)
  const eSafe: Effect.Effect<A, E, R & UseCases> = eLogged.pipe(
    Effect.tapError(error =>
      Effect.sync(() => log.error('Error in forked effect:', error))
    )
    /*
    Effect.catchAll((error) => {
      console.error('Unhandled error:', error)
      // Optionally set error state or retry
      return Effect.succeed(null)
    })*/
  )
  return appRuntime.runFork(eSafe)
};

export const interruptFiber = <A, E>(fiber: Fiber.Fiber<A, E>) =>
  Fiber.interrupt(tf(fiber)) as unknown as void;

export const syncAppEffect = <A, E, R>(
  effect: Effect.Effect<A, E, R & UseCases>
) =>
  appRuntime.runSync(te(effect));
