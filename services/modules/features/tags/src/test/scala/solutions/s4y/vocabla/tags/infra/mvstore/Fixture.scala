package solutions.s4y.vocabla.tags.infra.mvstore

import org.h2.mvstore.MVStore
import solutions.s4y.vocabla.id.IdFactory
import solutions.s4y.vocabla.infrastructure.mvstore.KeyValueMVStore.makeMVStoreMemory
import solutions.s4y.vocabla.infrastructure.mvstore.ToSegment
import solutions.s4y.vocabla.tags.app.repo.{
  TagAssociationRepository,
  TagRepository
}
import zio.{UIO, ULayer, ZIO, ZLayer}

object Fixture:
  type ID = Int

  given ToSegment[Fixture.ID] with
    override def apply(value: ID): String = value.toString

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

  def makeTagRepositoryLayer(): ZLayer[
    Any,
    String,
    TagRepository
  ] = {
    layerMVStore ++ layerIdFactory >>> MVStoreTagRepository
      .makeLayer[Fixture.ID, Fixture.ID]()
  }

  def makeTagAssociationRepositoryLayer[TaggedT: zio.Tag](name: String): ZLayer[
    Any,
    Serializable,
    TagAssociationRepository[TaggedT]
  ] = {
    layerMVStore ++ layerIdFactory >>> MVStoreTagAssociationRepository
      .makeLayer[Fixture.ID, Fixture.ID, TaggedT](name)
  }
