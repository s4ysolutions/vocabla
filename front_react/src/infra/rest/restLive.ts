import {Effect, Layer, Schema} from 'effect';
import {HttpClient} from '../http/HttpClient.ts';
import {REST} from './REST.ts';

export const restLive: Layer.Layer<REST, never, HttpClient> = Layer.effect(
  REST,
  Effect.gen(function* () {
    const request = yield* HttpClient;
    return {
      post: (url, body, schemaIn, schemaOut) =>
        request.execute('POST', url, JSON.stringify(Schema.encode(schemaIn)(body))).pipe(
          Effect.flatMap((response) => Schema.decodeUnknown(schemaOut)(response)),
        )
    };
  })
)
