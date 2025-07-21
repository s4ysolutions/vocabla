package solutions.s4y.vocabla.words.app.usecase

import org.h2.mvstore.MVStore
import solutions.s4y.vocabla.id.IdFactory
import solutions.s4y.vocabla.lang.app.repo.LangRepository
import solutions.s4y.vocabla.words.app.repo.EntryRepository
import solutions.s4y.vocabla.words.app.usecase.WordsServiceLive
import solutions.s4y.vocabla.words.infra.kv.mvstore.MVStoreRepository
import zio.ZLayer

object WordsServiceMVStore:
  def makeLayer[ID: zio.Tag]: ZLayer[
    MVStore & IdFactory[ID],
    String,
    WordsService
  ] =
    val wordsServiceLayer: ZLayer[
      EntryRepository,
      String,
      WordsServiceLive
    ] =
      ZLayer.fromFunction((repo: EntryRepository) => new WordsServiceLive(repo))

    MVStoreRepository.makeLayer[ID] >>> wordsServiceLayer
