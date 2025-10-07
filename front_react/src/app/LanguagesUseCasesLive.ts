import {type LanguagesUseCases, LanguagesUseCasesTag} from '../app-ports/LanguagesUseCases.ts';
import type {Lang} from '../domain/Lang.ts';
import type {AppError} from '../app-ports/errors/AppError.ts';
import type {LangCode} from '../domain/LangCode.ts';
import {Effect, Layer} from 'effect';
import {type AllLangsR, type LangRepository, LangRepositoryTag} from '../app-repo/LangRepository.ts';
import infra2appError from './infra2appError.ts';
import loglevel from 'loglevel';

const log = loglevel.getLogger('LanguagesUseCasesLive')
log.setLevel(loglevel.levels.DEBUG)

class LanguagesUseCasesLive implements LanguagesUseCases {
  readonly allLanguages: Effect.Effect<ReadonlyArray<Lang>, AppError>
  readonly defaultLang: Effect.Effect<Lang, AppError>
  readonly unknownLang: Effect.Effect<Lang, AppError>

  private constructor(
    private readonly allLangsEffect: Effect.Effect<AllLangsR, AppError>,
    private readonly langMapEffect: Effect.Effect<Map<LangCode, Lang>, AppError>
  ) {
    this.allLanguages = Effect.map(this.allLangsEffect, (r) => r.languages)
    this.defaultLang = Effect.map(this.allLangsEffect, (r) => r.defaultLang)
    this.unknownLang = Effect.map(this.allLangsEffect, (r) => r.unknownLang)
  }

  static make(langRepository: LangRepository, cached: boolean = true): Effect.Effect<LanguagesUseCasesLive, AppError> {
    const getAllLangsEffect: Effect.Effect<AllLangsR, AppError> =
      langRepository.getAllLangs().pipe(
        Effect.mapError(infra2appError)
      )

    if (cached)
      return Effect.gen(function* () {

        const cachedAllLangs = yield* Effect.cached(getAllLangsEffect)

        const mapEffect = Effect.map(cachedAllLangs, (r) =>
          new Map(r.languages.map(lang => [lang.code, lang]))
        )
        const cachedLangMap = yield* Effect.cached(mapEffect)
        return new LanguagesUseCasesLive(cachedAllLangs, cachedLangMap)
      })
    else {
      const mapEffect = Effect.map(getAllLangsEffect, (r) =>
        new Map(r.languages.map(lang => [lang.code, lang]))
      )
      return Effect.succeed(new LanguagesUseCasesLive(getAllLangsEffect, mapEffect))
    }
  }

  static readonly layer: Layer.Layer<LanguagesUseCasesTag, never, LangRepositoryTag> = Layer.effect(
    LanguagesUseCasesTag,
    Effect.flatMap(
      LangRepositoryTag, // Dependency tag
      (langRepository) => LanguagesUseCasesLive.make(langRepository)
    ).pipe(Effect.orDie)
  )

  getLangByCode(code: LangCode): Effect.Effect<Lang, AppError> {
    return Effect.all([this.langMapEffect, this.allLangsEffect]).pipe(
      Effect.map(([langMap, allLangs]) =>
        langMap.get(code) ?? allLangs.unknownLang
      )
    )
  }
}

export default LanguagesUseCasesLive
