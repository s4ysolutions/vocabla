/**
 * A live implementation of RestClient using HttpClient
 */
import {Effect, Layer} from 'effect';
import {type HttpClient, HttpClientTag} from '../http/HttpClient.ts';
import {type Get, type Post, type RestClient, RestClientTag} from './RestClient.ts';
import HttpClientLive from '../http/httpClientLive.ts';

export const restClient = (httpClient: HttpClient): RestClient => ({
  post: <REQ, RESP, OUT>({url, body, decoder}: Post<REQ, RESP, OUT>) =>
    Effect.flatMap(
      // get unknown response from http client
      httpClient.execute<REQ, RESP>('POST', url, body),
      decoder),
  get: <RESP, OUT>({url, decoder}: Get<RESP, OUT>) =>
    Effect.flatMap(
      // get unknown response from http client
      httpClient.execute<unknown, RESP>('GET', url),
      // decode unknown response to OUT
      decoder
    ),
  delete: <RESP, OUT>({url, decoder}: Get<RESP, OUT>) =>
    Effect.flatMap(
      // get unknown response from http client
      httpClient.execute<unknown, RESP>('DELETE', url),
      // decode unknown response to OUT
      decoder
    )
})

export const restClientLayer: Layer.Layer<RestClientTag, never, HttpClientTag> =
  Layer.effect(
    RestClientTag,
    Effect.gen(function* () {
      const httpClient = yield* HttpClientTag;
      return restClient(httpClient);
    }))

export const restClientLive: Layer.Layer<RestClientTag> = restClientLayer.pipe(
  Layer.provide(HttpClientLive)
)
