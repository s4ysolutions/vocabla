import {Effect, Layer} from 'effect';
import {type HttpClient, HttpClientTag} from '../http/HttpClient.ts';
import {type Get, type Post, type RestClient, RestClientTag} from './RestClient.ts';
import loglevel from 'loglevel';

const log = loglevel.getLogger('restClient')
log.setLevel(loglevel.levels.INFO)

class RestClientLive implements RestClient {
  private constructor(
    private readonly httpClient: HttpClient
  ) {
  }

  static make(httpClient: HttpClient): Effect.Effect<RestClientLive> {
    return Effect.succeed(new RestClientLive(httpClient));
  }

  static readonly layer: Layer.Layer<RestClientTag, never, HttpClientTag> = Layer.effect(
    RestClientTag,
    Effect.flatMap(
      HttpClientTag, // Dependency tag
      (httpClient) => RestClientLive.make(httpClient)
    )
  )

  post<REQ, RESP, OUT>({url, body, decoder}: Post<REQ, RESP, OUT>) {
    return Effect.flatMap(
      this.httpClient.execute<REQ, RESP>('POST', url, body),
      decoder)
  }

  get<RESP, OUT>({url, decoder}: Get<RESP, OUT>) {
    log.debug('RestClientLive GET ' + url)
    return Effect.flatMap(
      this.httpClient.execute<unknown, RESP>('GET', url),
      decoder
    )
  }

  delete<RESP, OUT>({url, decoder}: Get<RESP, OUT>) {
    return Effect.flatMap(
      this.httpClient.execute<unknown, RESP>('DELETE', url),
      decoder
    )
  }
}

export default RestClientLive;
