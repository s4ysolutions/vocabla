package solutions.s4y.vocabla.words.app.usecase

import org.h2.mvstore.MVStore
import solutions.s4y.vocabla.id.IdFactory
import solutions.s4y.vocabla.words.app.repo.{
  DtoIdToDomainId,
  EntryRepository,
  TagRepository
}
import solutions.s4y.vocabla.words.app.repo.dto.{EntryDTO, TagDTO}
import solutions.s4y.vocabla.words.app.usecase.WordsServiceLive
import solutions.s4y.vocabla.words.infra.kv.mvstore.{
  MVStoreEntryRepository,
  MVStoreTagRepository
}
import zio.{Tag, ZLayer}

object WordsServiceMVStore:
  def makeLayer[ID: Tag](using
      DtoIdToDomainId[ID, ID]
  ): ZLayer[
    MVStore & IdFactory[ID],
    String,
    WordsService[ID, ID, ID]
  ] =
    val mvStoreTagRepositoryLayer: ZLayer[
      MVStore & IdFactory[ID],
      String,
      MVStoreTagRepository[ID, ID]
    ] = MVStoreTagRepository.makeMVstoreLayer[ID, ID]

    val mvStoreEntryRepositoryLayer: ZLayer[
      MVStore & IdFactory[ID],
      String,
      MVStoreEntryRepository[ID, ID, ID]
    ] =
      mvStoreTagRepositoryLayer >>> MVStoreEntryRepository
        .makeMvStoreLayer[ID, ID, ID]

    val tagRepositoryLayer: ZLayer[
      MVStore & IdFactory[ID],
      String,
      TagRepository[ID, ID, TagDTO[ID]]
    ] = mvStoreTagRepositoryLayer >>> MVStoreTagRepository.makeLayer[ID, ID]

    val entryRepositoryLayer: ZLayer[
      MVStore & IdFactory[ID],
      String,
      EntryRepository[ID, ID, EntryDTO[ID, ID]]
    ] = mvStoreEntryRepositoryLayer >>> MVStoreEntryRepository
      .makeLayer[ID, ID, ID]

    val wordsServiceLayer: ZLayer[
      EntryRepository[ID, ID, EntryDTO[ID, ID]],
      String,
      WordsServiceLive[ID, ID, ID]
    ] =
      ZLayer.fromFunction((repo: EntryRepository[ID, ID, EntryDTO[ID, ID]]) =>
        new WordsServiceLive[ID, ID, ID](repo)
      )

    entryRepositoryLayer >>> wordsServiceLayer
