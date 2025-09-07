import {Effect, Layer, Option, Schema} from 'effect';
import {type RestClient, RestClientTag} from '../rest/restClient.ts';
import {type TagsRepository, TagsRepositoryTag} from '../../app-repo/TagsRepository.ts';
import {infraError, type InfraError} from '../../app-repo/infraError.ts';
import {tt} from '../../translable/Translatable.ts';
import type {ClientError} from '../http/errors/ClientError.ts';
import type {HTTPError} from '../http/errors/HTTPError.ts';
import type {JsonDecodingError} from '../http/errors/JsonDecodingError.ts';
import {type Tag} from '../../domain/Tag.ts';
import {type Identifier} from '../../domain/identity/Identifier.ts';
import type {components} from '../rest/types.ts';
import {ParseError} from 'effect/ParseResult';
import {typeFromProp} from './transformers/typeFromProp.ts';
import {identifierFromNumber} from './transformers/identifierFromNumber.ts';
import {tagFromDto} from './transformers/tag/tagFromDto.ts';
import {nullOrFromProp} from './transformers/nullOrFromProp.ts';

type CreateTagRequest = components['schemas']['CreateTagRequest']
export type CreateTagResponse = components['schemas']['CreateTagResponse'];
export const decodeCreateTagResponse = Schema.decodeUnknown(
  typeFromProp('tagId',
    identifierFromNumber<Tag>()))

export type GetTagResponse = components['schemas']['GetTagResponse']
export const decodeGetTagResponse = Schema.decodeUnknown(
  nullOrFromProp('tag', tagFromDto))

const urlBase = 'http://vocabla:3000/rest/v1'
//const urlBase = 'http://localhost:8080/rest/v1'

const repositoryRest = (rest: RestClient): TagsRepository => ({
  createTag: (tag) => {
    const request: CreateTagRequest = {tag: {label: tag.label, ownerId: tag.ownerId.value}}
    return Effect.mapError(
      rest.post<CreateTagRequest, Identifier<Tag>>({
        url: `${urlBase}/tags`,
        body: request,
        decoder: decodeCreateTagResponse,
      }), _error2infraError)
  },// end createTag
  getTag: (tagId) => {
    return Effect.mapError(
      rest.get<Option.Option<Tag>>({
        url: `${urlBase}/tags/${tagId.value}`,
        decoder: decodeGetTagResponse,
      }), _error2infraError)
  } // end getTag
})

const _error2infraError = (error: ClientError | HTTPError | JsonDecodingError | ParseError): InfraError => {
  if (error instanceof ParseError) {
    return infraError(tt`Parsing error: ${error.message}`, error)
  }
  switch (error._tag) {
    case 'ClientError':
      return infraError(error.message, error)
    case 'HTTPError':
      return infraError(tt`HTTP: ${error.status} ${error.statusText}`, error)
    case 'JsonDecodingError':
      return infraError(tt`Decoding error`, error)
    default: {
      const _exhaustive: never = error as never
      void _exhaustive;
      return _exhaustive
    }
  }
}

export const repositoryRestLayer: Layer.Layer<TagsRepositoryTag, never, RestClientTag> = Layer.effect(
  TagsRepositoryTag,
  Effect.map(RestClientTag, (rest) => repositoryRest(rest)
  ))
