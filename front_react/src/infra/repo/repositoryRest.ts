import {Effect, Option} from 'effect';
import {type RestClient} from '../rest/RestClient.ts';
import {type TagsRepository} from '../../app-repo/TagsRepository.ts';
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
import {decodeGetTagResponse, type GetTagResponse} from './dto/tag/GetTagResponse.ts';
import type {CreateEntryRequest} from './dto/entry/CreateEntryRequest.ts';
import {type Entry} from '../../domain/Entry.ts';
import {type CreateTagResponse, decodeCreateTagResponse} from './dto/tag/CreateTagResponse.ts';
import {type CreateEntryResponse, decodeCreateEntryResponse} from './dto/entry/CreateEntryResponse.ts';
import {decodeGetEntryResponse, type GetEntryResponse} from './dto/entry/GetEntryResponse.ts';
import {decodeGetEntriesResponse, type GetEntriesResponse} from './dto/entry/GetEntriesResponse.ts';
import type {Identified} from '../../domain/identity/Identified.ts';

const urlBase = 'http://vocabla:3000/rest/v1'
//const urlBase = 'http://localhost:8080/rest/v1'

export const repositoryRest = (restClient: RestClient): TagsRepository & EntriesRepository => ({
  createTag: (tag) => {
    const request: CreateTagRequest = {tag: {label: tag.label, ownerId: tag.ownerId}}
    return Effect.mapError(
      restClient.post<CreateTagRequest, CreateTagResponse, Identifier<Tag>>({
        url: `${urlBase}/tags`,
        body: request,
        decoder: decodeCreateTagResponse,
      }), _error2infraError)
  },// end createTag
  getTag: (tagId) => {
    return Effect.mapError(
      restClient.get<GetTagResponse, Option.Option<Tag>>({
        url: `${urlBase}/tags/${tagId}`,
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
        ownerId: entry.ownerId,
      },
      tagIds: tagIds
    }
    return Effect.mapError(
      restClient.post<CreateEntryRequest, CreateEntryResponse, Identifier<Entry>>({
        url: `${urlBase}/entries`,
        body: request,
        decoder: decodeCreateEntryResponse,
      }), _error2infraError
    )
  },
  getEntry: (entryId) => {
    return Effect.mapError(
      restClient.get<GetEntryResponse, Option.Option<Entry>>({
        url: `${urlBase}/entries/${entryId}`,
        decoder: decodeGetEntryResponse,
      }), _error2infraError)
  },
  getEntriesByOwner: (ownerId, filter) => {
    const queryParams = new URLSearchParams();
    queryParams.append('ownerId', ownerId.toString());

    if (filter.tagIds.length > 0) {
      filter.tagIds.forEach(tagId => queryParams.append('tagId', tagId.toString()));
    }

    if (filter.langCodes.length > 0) {
      filter.langCodes.forEach(langCode => queryParams.append('lang', langCode));
    }

    if (Option.isSome(filter.text)) {
      if (filter.text.value.trim().length > 0) {
        queryParams.append('text', filter.text.value);
      }
    }

    return Effect.mapError(
      restClient.get<GetEntriesResponse, {readonly entries: ReadonlyArray<Identified<Entry>>}>({
        url: `${urlBase}/entries?${queryParams.toString()}`,
        decoder: decodeGetEntriesResponse,
      }), _error2infraError)
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
