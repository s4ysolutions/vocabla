package solutions.s4y.vocabla.words.infra.kv.mvstore

import org.h2.mvstore.MVStore
import solutions.s4y.vocabla.id.IdFactory
import solutions.s4y.vocabla.infrastructure.mvstore.KeyValueMVStore.makeMVStoreMemory
import solutions.s4y.vocabla.words.app.repo.{
  DtoIdToDomainId,
  EntryRepository,
  TagRepository
}
import solutions.s4y.vocabla.words.app.repo.dto.{EntryDTO, TagDTO}
import zio.{UIO, ULayer, ZIO, ZLayer}

object Fixture:
  opaque type ID = Int
  object ID:
    def apply(value: Int): ID = value

    given CanEqual[ID, ID] = CanEqual.derived
    given CanEqual[ID, Int] = CanEqual.derived
    given CanEqual[Int, ID] = CanEqual.derived

    extension (id: ID) def value: Int = id

    given Conversion[Int, ID] with
      def apply(value: Int): ID = ID(value)

    given Conversion[ID, Int] with
      def apply(id: ID): Int = id.value

    given DtoIdToDomainId[ID, ID] with
      def toDomain(dtoId: ID): ID = dtoId

  def layerIdFactory: ULayer[IdFactory[Fixture.ID]] =
    ZLayer.succeed(new IdFactory[Fixture.ID]:
      private var currentId = 10

      override def next: UIO[Fixture.ID] = ZIO.succeed {
        currentId += 1
        Fixture.ID(currentId)
      })

  def layerMVStore: ZLayer[Any, String, MVStore] =
    ZLayer.scoped(
      makeMVStoreMemory().mapError(th =>
        s"Failed to create MVStore: ${th.getMessage}"
      )
    )

  def layerTagRepository: ZLayer[
    Any,
    Serializable,
    TagRepository[Fixture.ID, Fixture.ID, TagDTO[Fixture.ID]]
  ] =
    val layer1 = layerMVStore ++ layerIdFactory
    val layer2 =
      layer1 >>> MVStoreTagRepository.makeMVstoreLayer[Fixture.ID, Fixture.ID]
    (layer1 ++ layer2) >>> MVStoreTagRepository
      .makeLayer[Fixture.ID, Fixture.ID]

  def layerEntryRepository: ZLayer[
    Any,
    Serializable,
    EntryRepository[Fixture.ID, Fixture.ID, EntryDTO[Fixture.ID, Fixture.ID]]
  ] =
    val layer1: ZLayer[Any, String, MVStore & IdFactory[Fixture.ID]] =
      layerMVStore ++ layerIdFactory

    val layer2
        : ZLayer[Any, String, MVStoreTagRepository[Fixture.ID, Fixture.ID]] =
      layer1 >>> MVStoreTagRepository.makeMVstoreLayer[Fixture.ID, Fixture.ID]

    val layer3: ZLayer[
      Any,
      String,
      MVStoreEntryRepository[Fixture.ID, Fixture.ID, Fixture.ID]
    ] =
      (layer1 ++ layer2) >>> MVStoreEntryRepository
        .makeMvStoreLayer[Fixture.ID, Fixture.ID, Fixture.ID]

    val layer4: ZLayer[
      MVStoreEntryRepository[Fixture.ID, Fixture.ID, Fixture.ID],
      String,
      EntryRepository[Fixture.ID, Fixture.ID, EntryDTO[Fixture.ID, Fixture.ID]]
    ] =
      layer1 >>> MVStoreEntryRepository
        .makeLayer[Fixture.ID, Fixture.ID, Fixture.ID]

    layer3 >>> layer4
