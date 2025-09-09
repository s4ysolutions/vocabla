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
import {ParseError} from 'effect/ParseResult';
import type {EntriesRepository} from '../../app-repo/EntriesRepository.ts';
import type {CreateTagRequest} from './dto/tag/CreateTagRequest.ts';
import {decodeCreateTagResponse} from './dto/entry/CreateTagResponse.ts';
import {decodeGetTagResponse} from './dto/tag/GetTagResponse.ts';
import type {CreateEntryRequest} from './dto/entry/CreateEntryRequest.ts';
import {definition, type Entry} from '../../domain/Entry.ts';

const urlBase = 'http://vocabla:3000/rest/v1'
//const urlBase = 'http://localhost:8080/rest/v1'

const repositoryRest = (rest: RestClient): TagsRepository & EntriesRepository => ({
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
  }, // end getTag
  createEntry: (entry, tagIds) => {
    const request: CreateEntryRequest = {
      entry: {
        headword: {word: entry.word.s, langCode: entry.word.langCode},
        definitions: entry.definitions.map(definition => ({
          definition: definition.localized.s,
          langCode: definition.localized.langCode
        })),
        ownerId: entry.ownerId.value,
      },
      tagIds: tagIds.map(t => t.value)
    }
    return Effect.mapError(
      rest.post<CreateEntryRequest, Identifier<Entry>>({
        url: `${urlBase}/entries`,
        body: request,
        decoder: Schema.number.pipe(Schema.int).transform(
          id => new Identifier(id),
          id => id.value
        ),
      }), _error2infraError
    )
  },
  getEntry: (entryId) => {
    throw new Error('Method not implemented.');
  }
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
