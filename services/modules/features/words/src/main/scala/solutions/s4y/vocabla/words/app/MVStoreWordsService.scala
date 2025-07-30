package solutions.s4y.vocabla.words.app

import org.h2.mvstore.MVStore
import solutions.s4y.vocabla.id.IdFactory
import solutions.s4y.vocabla.words.app.WordsServiceLive
import solutions.s4y.vocabla.words.app.ports.WordsService
import solutions.s4y.vocabla.words.app.repo.EntryRepository
import solutions.s4y.vocabla.words.infra.mvstore.MVStoreEntryRepository
import zio.ZLayer

object MVStoreWordsService:
  def makeLayer[DtoID: zio.Tag]: ZLayer[
    MVStore & IdFactory[DtoID],
    String,
    WordsService
  ] = MVStoreEntryRepository.makeLayer >>> ZLayer.fromFunction(
    (repo: EntryRepository) => new WordsServiceLive(repo)
  )
