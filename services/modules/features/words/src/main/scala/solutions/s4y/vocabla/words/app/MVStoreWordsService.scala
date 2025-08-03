package solutions.s4y.vocabla.words.app
/*
import org.h2.mvstore.MVStore
import solutions.s4y.vocabla.id.IdFactory
import solutions.s4y.vocabla.words.app.EntryService
import solutions.s4y.vocabla.words.app.ports.EntryService
import solutions.s4y.vocabla.words.app.repo.EntryRepository
import solutions.s4y.vocabla.words.infra.mvstore.MVStoreEntryRepository
import zio.ZLayer

object MVStoreWordsService:
  def makeLayer[DtoID: zio.Tag]: ZLayer[
    MVStore & IdFactory[DtoID],
    String,
    EntryService
  ] = MVStoreEntryRepository.makeLayer >>> ZLayer.fromFunction(
    (repo: EntryRepository) => new EntryService(repo)
  )
*/