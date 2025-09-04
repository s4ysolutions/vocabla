import * as Effect from "effect/Effect";
import * as ManagedRuntime from "effect/ManagedRuntime";
import { WordsAdapterRestLayer } from "../infra/rest-adapter.ts";
// import * as Layer from "effect/Layer"; // Uncomment when merging layers
// import { UserServiceLayer } from "./example-services"; // Add when needed

// Combine all application layers here
// Currently only WordsAdapter, but you can easily add more:
const AppLayer = WordsAdapterRestLayer;
// Later when you need more services:
// const AppLayer = Layer.merge(WordsAdapterRestLayer, UserServiceLayer, ConfigLayer);

// Create a ManagedRuntime with all layers provided
const AppRuntime = ManagedRuntime.make(AppLayer);

// Universal function to run ANY effect with all app layers provided
export const runAppEffect = <A, E, R>(
  effect: Effect.Effect<A, E, R>
): Promise<A> =>
  AppRuntime.runPromise(effect as Effect.Effect<A, E, never>);

// For effects that need custom error handling
export const runAppEffectExit = <A, E, R>(
  effect: Effect.Effect<A, E, R>
) =>
  AppRuntime.runPromiseExit(effect as Effect.Effect<A, E, never>);

// Export the runtime itself if needed
export { AppRuntime, AppLayer };
