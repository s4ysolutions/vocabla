import {Effect, Layer, Schema} from 'effect';
import {type HttpClient, HttpClientTag} from '../http/HttpClient.ts';
import {type Post, type RestClient, RestClientTag} from './restClient.ts';

export const restClientLive = (httpClient: HttpClient): RestClient => ({
  post: <IN, OUT, Odto>({url, body, schemaOut}: Post<IN, OUT, Odto>) =>
    Effect.flatMap(
      // get unknown response from http client
      httpClient.execute('POST', url, body),
      // decode unknown response to OUT
      Schema.decodeUnknown(schemaOut))
  //end post
})

export const restClientLayer: Layer.Layer<RestClientTag, never, HttpClientTag> =
  Layer.effect(RestClientTag, HttpClientTag.pipe(Effect.map(restClientLive)));
