import {describe, it} from '@effect/vitest';
import {Context, Effect, Layer} from 'effect';
import {assertEquals} from '@effect/vitest/utils';

describe('layers', () => {
  let constructionCount = 0;
  let callCount = 0;

  interface Provider {
    foo(): void;
  }

  class ProviderTag extends Context.Tag('Provider')<ProviderTag, Provider>() {
  }

  class ProviderLive implements Provider {
    constructor() {
      constructionCount++;
      console.log('ProviderLive constructed: ', constructionCount);
    }

    foo(): void {
      callCount++;
    }

    static readonly layer = Layer.sync(ProviderTag, () => new ProviderLive());
  }

  interface ConsumerA {
    bar(): void;
  }

  class ConsumerATag extends Context.Tag('ConsumerA')<ConsumerATag, ConsumerA>() {
  }

  class ConsumerALive implements ConsumerA {
    constructor(private provider: Provider) {
      //constructionCount++;
    }

    bar(): void {
      this.provider.foo();
    }

    static readonly layer = Layer.effect(
      ConsumerATag,
      Effect.map(ProviderTag, (provider) =>
        new ConsumerALive(provider)
      )
    );
  }

  interface ConsumerB {
    baz(): void;
  }

  class ConsumerBTag extends Context.Tag('ConsumerB')<ConsumerBTag, ConsumerB>() {
  }

  class ConsumerBLive implements ConsumerB {
    constructor(private provider: Provider) {
      //constructionCount++;
    }

    baz(): void {
      this.provider.foo();
    }

    static readonly layer = Layer.effect(
      ConsumerBTag,
      Effect.map(ProviderTag, (provider) =>
        new ConsumerBLive(provider)
      )
    );
  }

  it('dependenty layer constructed once', () => {
    constructionCount = 0;
    callCount = 0;

    const appLayer = Layer.merge(
      Layer.provide(ConsumerALive.layer, ProviderLive.layer),
      Layer.provide(ConsumerBLive.layer, ProviderLive.layer)
    );

    const program = Effect.gen(function* () {
      const consumerA = yield* ConsumerATag;
      const consumerB = yield* ConsumerBTag;
      consumerA.bar();
      consumerB.baz();
    }).pipe(Effect.provide(appLayer));

    Effect.runSync(program);

    assertEquals(constructionCount, 1)
    assertEquals(callCount, 2)
  })
})
