import { useCallback } from "react";
import * as Effect from "effect/Effect";
import { runAppEffect, runAppEffectExit } from "../../../infra/effect-runtime";
import { WordsRepositoryTag } from "../../words-manager/application/repo/words-repository";

/**
 * Hook for running Effects with centralized layer management
 */
export const useAppEffect = () => {
  const runEffect = useCallback(
    <A, E>(effect: Effect.Effect<A, E, WordsRepositoryTag>): Promise<A> =>
      runAppEffect(effect),
    []
  );

  const runEffectExit = useCallback(
    <A, E>(effect: Effect.Effect<A, E, WordsRepositoryTag>) =>
      runAppEffectExit(effect),
    []
  );

  return { runEffect, runEffectExit };
};

export default useAppEffect;
