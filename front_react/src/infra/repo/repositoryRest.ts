import {Effect, Layer, Schema} from 'effect';
import {type RestClient, RestClientTag} from '../rest/restClient.ts';
import {type TagsRepository, TagsRepositoryTag} from '../../app-repo/TagsRepository.ts';
import {infraError, type InfraError} from '../../app-repo/infraError.ts';
import {tt} from '../../translable/Translatable.ts';
import type {ClientError} from '../http/errors/ClientError.ts';
import type {HTTPError} from '../http/errors/HTTPError.ts';
import type {JsonDecodingError} from '../http/errors/JsonDecodingError.ts';
import {type Tag} from '../../domain/Tag.ts';
import {type Identifier, schemaIdentifier} from '../../domain/identity/Identifier.ts';
import type {components} from '../rest/types.ts';
import type {ParseError} from 'effect/ParseResult';

type CreateTagRequest = components['schemas']['CreateTagRequest']
type CreateTagResponse = components['schemas']['CreateTagResponse']

const schemaCreateTagResponse: Schema.Schema<Identifier<Tag>, CreateTagResponse> = Schema.transform(
  Schema.Struct({tagId: Schema.Number}),
  schemaIdentifier<Tag>(),
  {
    strict: true,
    decode: (res) => ({value: res.tagId}),
    encode: (id) => ({tagId: id.value})
  }
)

//const urlBase = 'http://vocabla:3000/rest/v1'
const urlBase = 'http://localhost:8080/rest/v1'

const repositoryRest = (rest: RestClient): TagsRepository => ({
  createTag: (tag) => {
    const request: CreateTagRequest = {tag: {label: tag.label, ownerId: tag.ownerId.value}}
    return Effect.mapError(
      rest.post<CreateTagRequest, Identifier<Tag>, CreateTagResponse>({
        url: `${urlBase}/tags`,
        body: request,
        schemaOut: schemaCreateTagResponse,
      }), _error2infraError)
  }// end createTag
})

const _error2infraError = (error: ClientError | HTTPError | JsonDecodingError | ParseError): InfraError => {
  switch (error._tag) {
    case 'ClientError':
      return infraError(error.message, error)
    case 'HTTPError':
      return infraError(tt`HTTP: ${error.status} ${error.statusText}`, error)
    case 'JsonDecodingError':
      return infraError(tt`Decoding error`, error)
    default: {
      // noinspection UnnecessaryLocalVariableJS
      const _exhaustive: never = error as never
      return _exhaustive
    }
  }
}

export const repositoryRestLayer: Layer.Layer<TagsRepositoryTag, never, RestClientTag> = Layer.effect(
  TagsRepositoryTag,
  Effect.map(RestClientTag, (rest) => repositoryRest(rest)
  ))
