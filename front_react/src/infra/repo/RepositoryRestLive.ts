import {Context, Effect, Layer, Option} from 'effect';
import {type RestClient, RestClientTag} from '../rest/RestClient.ts';
import {InfraError} from '../../app-repo/InfraError.ts';
import {tt} from '../../translable/Translatable.ts';
import type {ClientError} from '../http/errors/ClientError.ts';
import type {HTTPError} from '../http/errors/HTTPError.ts';
import type {JsonDecodingError} from '../http/errors/JsonDecodingError.ts';
import {type Identifier} from '../../domain/identity/Identifier.ts';
import {ParseError} from 'effect/ParseResult';
import {type EntriesRepository, EntriesRepositoryTag} from '../../app-repo/EntriesRepository.ts';
import type {CreateEntryRequest} from './dto/entry/CreateEntryRequest.ts';
import {type Entry} from '../../domain/Entry.ts';
import {type CreateTagResponseDto, decodeCreateTagResponse} from './dto/learning_settings/CreateTagResponseDto.ts';
import {type CreateEntryResponse, decodeCreateEntryResponse} from './dto/entry/CreateEntryResponse.ts';
import {decodeGetEntryResponse, type GetEntryResponse} from './dto/entry/GetEntryResponse.ts';
import {decodeGetEntriesResponse, type GetEntriesResponseDto} from './dto/entry/GetEntriesResponse.ts';
import type {Identified} from '../../domain/identity/Identified.ts';
import {type LangRepository, LangRepositoryTag} from '../../app-repo/LangRepository.ts';
import type {Lang} from '../../domain/Lang.ts';
import {decodeGetLanguagesResponse, type GetLanguagesResponseDto} from './dto/lang/getLanguagesResponse.ts';
import {
  type LearningSettingsR,
  type LearningSettingsRepository,
  LearningSettingsRepositoryTag
} from '../../app-repo/LearningSettingsRepository.ts';
import {
  decodeGetLearningSettingsResponse,
  type GetLearningSettingsResponseDto,
} from './dto/learning_settings/GetLearningSettingsResponse.ts';
import {
  type AddKnownLangResponseDto,
  decodeAddKnownLangResponse
} from './dto/learning_settings/AddKnownLangResponse.ts';
import type {AddLearnLangResponseDto} from './dto/learning_settings/AddLearnLangResponse.ts';
import {
  decodeRemoveKnownLangResponse,
  type RemoveKnownLangResponseDto
} from './dto/learning_settings/RemoveKnownLangResponse.ts';
import {
  decodeRemoveLearnLangResponse,
  type RemoveLearnLangResponseDto
} from './dto/learning_settings/RemoveLearnLangResponse.ts';
import type {CreateTagRequestDto} from './dto/learning_settings/CreateTagRequestDto.ts';
import logLevel from 'loglevel';
import type {EntriesFilter} from '../../domain/EntriesFilter.ts';

const log = logLevel.getLogger('repositoryRest')
log.setLevel(logLevel.levels.DEBUG)

const urlBase = 'http://vocabla:3000/rest/v1'

//const urlBase = 'http://localhost:8080/rest/v1'

class RepositoryRestLive implements EntriesRepository, LangRepository, LearningSettingsRepository {
  private constructor(
    private readonly restClient: RestClient
  ) {
  }

  static make(restClient: RestClient): Effect.Effect<RepositoryRestLive> {
    return Effect.succeed(new RepositoryRestLive(restClient));
  }

  static readonly layer: Layer.Layer<EntriesRepositoryTag | LangRepositoryTag | LearningSettingsRepositoryTag, never, RestClientTag> = Layer.effectContext(
    Effect.gen(function* () {
      const restClient = yield* RestClientTag
      const repositoryImpl = yield* RepositoryRestLive.make(restClient)

      return Context.empty()
        .pipe(Context.add(EntriesRepositoryTag, repositoryImpl))
        .pipe(Context.add(LangRepositoryTag, repositoryImpl))
        .pipe(Context.add(LearningSettingsRepositoryTag, repositoryImpl))
    })
  )

  // EntriesRepository methods
  createEntry(entry: Entry, tagIds: ReadonlyArray<number>) {
    const request: CreateEntryRequest = {
      entry: {
        headword: {word: entry.word.s, langCode: entry.word.langCode},
        definitions: entry.definitions.map(definition => ({
          definition: definition.localized.s,
          langCode: definition.localized.langCode
        })),
        ownerId: entry.ownerId,
      },
      tagIds: tagIds as number[]
    }
    return Effect.mapError(
      this.restClient.post<CreateEntryRequest, CreateEntryResponse, Identifier<Entry>>({
        url: `${urlBase}/entries`,
        body: request,
        decoder: decodeCreateEntryResponse,
      }), _error2infraError
    )
  }

  getEntry(entryId: number) {
    return Effect.mapError(
      this.restClient.get<GetEntryResponse, Option.Option<Entry>>({
        url: `${urlBase}/entries/${entryId}`,
        decoder: decodeGetEntryResponse,
      }), _error2infraError)
  }

  getEntriesByOwner(ownerId: number, filter: EntriesFilter) {
    const queryParams = new URLSearchParams();
    queryParams.append('ownerId', ownerId.toString());

    if (filter.tagIds.length > 0) {
      filter.tagIds.forEach((tagId: number) => queryParams.append('tagId', tagId.toString()));
    }

    if (filter.langCodes.length > 0) {
      filter.langCodes.forEach((langCode: string) => queryParams.append('lang', langCode));
    }

    if (Option.isSome(filter.text)) {
      if (filter.text.value.trim().length > 0) {
        queryParams.append('text', filter.text.value);
      }
    }

    return Effect.mapError(
      this.restClient.get<GetEntriesResponseDto, { readonly entries: ReadonlyArray<Identified<Entry>> }>({
        url: `${urlBase}/entries?${queryParams.toString()}`,
        decoder: decodeGetEntriesResponse,
      }), _error2infraError)
  }

  // LangRepository methods
  getAllLangs() {
    return Effect.mapError(
      this.restClient.get<GetLanguagesResponseDto, {
        readonly defaultLang: Lang,
        readonly unknownLang: Lang,
        readonly languages: ReadonlyArray<Lang>
      }>({
        url: `${urlBase}/languages`,
        decoder: decodeGetLanguagesResponse,
      }), _error2infraError)
  }

  // LearningSettingsRepository methods
  getLearningSettings(studentId: number): Effect.Effect<LearningSettingsR, InfraError> {
    return Effect.mapError(
      this.restClient.get<GetLearningSettingsResponseDto, LearningSettingsR>({
        url: `${urlBase}/students/${studentId}/learning-settings`,
        decoder: decodeGetLearningSettingsResponse
      }), _error2infraError)
  }

  addKnownLang(studentId: number, langCode: string) {
    return Effect.mapError(
      this.restClient.post<null, AddKnownLangResponseDto, LearningSettingsR>({
        url: `${urlBase}/students/${studentId}/learning-settings/known-languages/${langCode}`,
        body: null,
        decoder: decodeAddKnownLangResponse
      }), _error2infraError)
  }

  removeKnownLang(studentId: number, langCode: string) {
    return Effect.mapError(
      this.restClient.delete<RemoveKnownLangResponseDto, LearningSettingsR>({
        url: `${urlBase}/students/${studentId}/learning-settings/known-languages/${langCode}`,
        decoder: decodeRemoveKnownLangResponse
      }), _error2infraError)
  }

  addLearnLang(studentId: number, langCode: string) {
    return Effect.mapError(
      this.restClient.post<null, AddLearnLangResponseDto, LearningSettingsR>({
        url: `${urlBase}/students/${studentId}/learning-settings/learn-languages/${langCode}`,
        body: null,
        decoder: decodeGetLearningSettingsResponse
      }), _error2infraError)
  }

  removeLearnLang(studentId: number, langCode: string) {
    return Effect.mapError(
      this.restClient.delete<RemoveLearnLangResponseDto, LearningSettingsR>({
        url: `${urlBase}/students/${studentId}/learning-settings/learn-languages/${langCode}`,
        decoder: decodeRemoveLearnLangResponse
      }), _error2infraError)
  }

  createTag(studentId: number, tag: CreateTagRequestDto) {
    return Effect.mapError(
      this.restClient.post<CreateTagRequestDto, CreateTagResponseDto, LearningSettingsR>({
        url: `${urlBase}/students/${studentId}/learning-settings/tags`,
        body: tag,
        decoder: decodeCreateTagResponse
      }), _error2infraError)
  }

  deleteTag(studentId: number, tagId: number) {
    return Effect.mapError(
      this.restClient.delete<GetLearningSettingsResponseDto, LearningSettingsR>({
        url: `${urlBase}/students/${studentId}/learning-settings/tags/${tagId}`,
        decoder: decodeGetLearningSettingsResponse
      }), _error2infraError)
  }
}

const _error2infraError = (error: ClientError | HTTPError | JsonDecodingError | ParseError): InfraError => {
  log.error('RepositoryRestLive error:', error)
  if (error instanceof ParseError) {
    return InfraError(tt`Parsing error: ${error.message}`, error)
  }
  switch (error._tag) {
    case 'ClientError':
      return InfraError(error.message, error)
    case 'HTTPError':
      return InfraError(tt`HTTP: ${error.status} ${error.statusText}`, error)
    case 'JsonDecodingError':
      return InfraError(tt`Decoding error`, error)
    default: {
      const _exhaustive: never = error as never
      void _exhaustive;
      return _exhaustive
    }
  }
}

export default RepositoryRestLive
