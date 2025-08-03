package solutions.s4y.vocabla.words.infra.mvstore

import org.h2.mvstore.MVStore
import solutions.s4y.vocabla.id.IdFactory
import solutions.s4y.vocabla.infrastructure.mvstore.KeyValueMVStore.makeMVStoreMemory
import solutions.s4y.vocabla.lang.app.repo.LangRepository
import solutions.s4y.vocabla.lang.infra.given
import solutions.s4y.vocabla.tags.app.repo.TagRepository
import solutions.s4y.vocabla.tags.infra.mvstore.MVStoreTagRepository
import solutions.s4y.vocabla.words.app.repo.EntryRepository
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
    val dep = layerMVStore ++ layerIdFactory
    val entries = dep >>> MVStoreEntryRepository.makeLayer[Fixture.ID]()
    val tags = dep >>> MVStoreTagRepository.makeLayer[Fixture.ID]()
    entries ++ tags
  }
