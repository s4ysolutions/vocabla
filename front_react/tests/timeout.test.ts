import {describe, expect, it} from '@effect/vitest';
import {Duration, Effect} from 'effect';


describe('timeout', () => {
  it.live('live', () => Effect.gen(function* () {
    const timestamp0 =  new Date().getTime()
    console.log('timestamp0', timestamp0, new Date())
    yield* Effect.sleep(Duration.millis(100))
    const timestamp1 = new Date().getTime()
    console.log('timestamp1', timestamp1, new Date())
    expect(timestamp1 - timestamp0).gt(50)
  }))
  it('none', () => Effect.gen(function* () {
    expect(true).toBe(false)
    const timestamp0 =  new Date().getTime()
    console.log('timestamp0', timestamp0, new Date())
    yield* Effect.sleep(Duration.millis(100))
    const timestamp1 = new Date().getTime()
    console.log('timestamp1', timestamp1, new Date())
    expect(timestamp1 - timestamp0).gt(50)
  }))
})
