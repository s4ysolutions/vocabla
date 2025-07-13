import { describe, it, expect } from "@effect/vitest";
import restAdapter from "../../src/infra/rest-adapter";
import { Effect, Exit } from "effect"

describe("rest.addEntry", () => {
  it.effect("returns hello (with it.effect)", () =>
    Effect.gen(function* () {
      const result = yield* Effect.succeed("hello");
      expect(result).toBe("hello");
    })
  );
  it.effect("creates a new entry and returns an ID failed", () =>
    Effect.gen(function* () {
      const result = yield* Effect.exit(restAdapter.addEntry(
        "test-owner-id",
        "word",
        "en",
        "definition",
        "en",
        ["tag1", "tag2"]
      ));
      expect(result).toStrictEqual( Exit.fail("HTTP error (400) POST /words/entries: Invalid Long: test-owner-id"));
    })
  );
  it.effect("creates a new entry and returns an ID success", () =>
    Effect.gen(function* () {
      const result = yield* restAdapter.addEntry(
        "0",
        "word",
        "en",
        "definition",
        "en",
        ["tag1", "tag2"]
      );
      expect(typeof result).toBe("string");
      expect(() => BigInt(result)).not.toThrow();
    })
  );
});