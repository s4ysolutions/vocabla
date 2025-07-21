package solutions.s4y.vocabla.words.infra.kv.mvstore

import org.h2.mvstore.MVStore
import solutions.s4y.vocabla.domain.model.Identifier
import solutions.s4y.vocabla.id.IdFactory
import solutions.s4y.vocabla.lang.app.repo.LangRepository
import solutions.s4y.vocabla.words.app.repo.{EntryRepository, TagRepository}
import zio.ZLayer

import java.util.UUID

object MVStoreRepository {

  def makeLayer[ID: zio.Tag]: ZLayer[
    MVStore & IdFactory[ID],
    String,
    EntryRepository & TagRepository
  ] = {
    type DEP = MVStore & IdFactory[ID]
    val tagRepositoryMVStore
        : ZLayer[DEP, String, MVStoreTagRepository[ID, ID]] =
      MVStoreTagRepository
        .makeMVstoreLayer[ID, ID]

    val tagRepository: ZLayer[
      DEP,
      String,
      TagRepository
    ] =
      tagRepositoryMVStore >>> MVStoreTagRepository
        .makeLayer[ID, ID]

    val entryRepositoryMVStore: ZLayer[
      DEP,
      String,
      MVStoreEntryRepository[ID, ID, ID]
    ] =
      tagRepositoryMVStore >>> MVStoreEntryRepository
        .makeMvStoreLayer[ID, ID, ID]

    val entryRepository: ZLayer[
      DEP,
      String,
      EntryRepository
    ] =
      entryRepositoryMVStore >>> MVStoreEntryRepository
        .makeLayer[ID, ID, ID]

    entryRepository ++ tagRepository
  }
}
