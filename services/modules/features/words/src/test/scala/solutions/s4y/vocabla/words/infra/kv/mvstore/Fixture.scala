package solutions.s4y.vocabla.words.infra.kv.mvstore

import org.h2.mvstore.MVStore
import solutions.s4y.vocabla.id.IdFactory
import solutions.s4y.vocabla.infrastructure.mvstore.KeyValueMVStore.makeMVStoreMemory
import solutions.s4y.vocabla.lang.app.repo.LangRepository
import solutions.s4y.vocabla.lang.infra.given
import solutions.s4y.vocabla.words.app.repo.{EntryRepository, TagRepository}
import zio.{UIO, ULayer, ZIO, ZLayer}

object Fixture:
  type ID = Int

  def layerIdFactory: ULayer[IdFactory[Fixture.ID]] =
    ZLayer.succeed(new IdFactory[Fixture.ID]:
      private var currentId = 10

      override def next: UIO[Fixture.ID] = ZIO.succeed {
        currentId += 1
        currentId
      })

  def layerMVStore: ZLayer[Any, String, MVStore] =
    ZLayer.scoped(
      makeMVStoreMemory().mapError(th =>
        s"Failed to create MVStore: ${th.getMessage}"
      )
    )

  def layerTestRepository: ZLayer[
    Any,
    Serializable,
    EntryRepository & TagRepository
  ] = {
    val layerMVStoreAndIdFactory
        : ZLayer[Any, String, MVStore & IdFactory[Fixture.ID]] =
      layerMVStore ++ layerIdFactory

    layerMVStoreAndIdFactory >>> MVStoreRepository.makeLayer[Fixture.ID]
  }
