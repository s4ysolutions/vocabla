package solutions.s4y.vocabla.infra.mvstore

import org.h2.mvstore.MVStore
import org.h2.mvstore.tx.TransactionStore
import solutions.s4y.infra.id.IdFactory
import solutions.s4y.infra.mvstore.ZMVStore.makeMVStoreMemory
import solutions.s4y.infra.sk.ToSegment
import solutions.s4y.vocabla.app.repo.{EntryRepository, TagRepository}
import zio.{UIO, ULayer, ZIO, ZLayer}

object Fixture:
  type ID = Int

  given ToSegment[Fixture.ID] with
    override def apply(value: ID): String = value.toString

  def layerIdFactory(): ULayer[IdFactory[Fixture.ID]] =
    ZLayer.succeed(new IdFactory[Fixture.ID]:
      private var currentId = 10

      override def next: UIO[Fixture.ID] = ZIO.succeed {
        currentId += 1
        currentId
      })

  def layerMVStore(): ZLayer[Any, String, MVStore] =
    ZLayer.scoped(
      makeMVStoreMemory().mapError(th =>
        s"Failed to create MVStore: ${th.getMessage}"
      )
    )

  def makeTagRepositoryLayer(): ZLayer[
    Any,
    String,
    TagRepository
  ] =
    layerMVStore() ++ layerIdFactory() >>> TagRepositoryMVStore
      .makeLayer[Fixture.ID, Fixture.ID]()

  def makeEntryRepositoryLayer(): ZLayer[
    Any,
    String,
    EntryRepository
  ] =
    layerMVStore() ++ layerIdFactory() >>> EntryRepositoryMVStore
      .makeLayer[Fixture.ID, Fixture.ID]()
