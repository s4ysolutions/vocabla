import { describe, it, expect } from "@effect/vitest";
import * as Effect from "effect/Effect";

describe("HelloWorld", () => {
  it("returns hello", () =>
    Effect.succeed("hello").pipe(
      Effect.map((result) => {
        expect(result).toEqual("hello");
      })
    ));
  it.effect("returns hello (with it.effect)", () =>
    Effect.gen(function* () {
      const result = yield* Effect.succeed("hello");
      expect(result).toBe("hello");
    })
  );
});
