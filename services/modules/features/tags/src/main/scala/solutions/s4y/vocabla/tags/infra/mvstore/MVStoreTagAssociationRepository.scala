package solutions.s4y.vocabla.tags.infra.mvstore

import org.h2.mvstore.MVStore
import solutions.s4y.vocabla.domain.model.Identifier
import solutions.s4y.vocabla.infrastructure.mvstore.SegmentedKey.given
import solutions.s4y.vocabla.infrastructure.mvstore.{
  SkZMVMap,
  ToSegment,
  ZMVMap
}
import solutions.s4y.vocabla.tags.app.repo.TagAssociationRepository
import solutions.s4y.vocabla.tags.domain.Tag
import zio.{IO, ZIO, ZLayer}

private final class MVStoreTagAssociationRepository[
    TagID: ToSegment, // DTO
    TaggedID: ToSegment, // DTO
    TaggedT // Domain type
] private (
    val mapTagTagged: SkZMVMap[TaggedID],
    val mapTaggedTag: SkZMVMap[TagID]
) extends TagAssociationRepository[TaggedT]:

  override def associateTagWithEntry(
      tagId: Identifier[Tag],
      taggedId: Identifier[TaggedT]
  ): IO[String, Unit] =
    (mapTagTagged.put(
      tagId.as[TagID] :: taggedId.as[TaggedID],
      taggedId.as[TaggedID]
    )
      <&>
        mapTaggedTag.put(
          taggedId.as[TaggedID] :: tagId.as[TagID],
          tagId.as[TagID]
        )).unit

  override def disassociateTagFromEntry(
      tagId: Identifier[Tag],
      taggedId: Identifier[TaggedT]
  ): IO[String, Unit] =
    (mapTagTagged.remove(tagId.as[TagID] :: taggedId.as[TaggedID])
      <&>
        mapTaggedTag.remove(tagId.as[TaggedID] :: taggedId.as[TagID])).unit

  override def disassociateTagFromAll(
      tagId: Identifier[Tag]
  ): IO[String, Unit] =
    mapTagTagged
      .cursorOf(tagId.as[TagID])
      .map(kv =>
        mapTagTagged.remove(kv._1) <&>
          mapTaggedTag.remove(kv._2 :: tagId.as[TagID])
      )
      .runCollect
      .flatMap(results => ZIO.collectAll(results).unit)

  override def disassociateTaggedFromAll(
      taggedId: Identifier[TaggedT]
  ): IO[String, Unit] =
    mapTaggedTag
      .cursorOf(taggedId.as[TaggedID])
      .map(kv =>
        mapTaggedTag.remove(kv._1) <&>
          mapTagTagged.remove(taggedId.as[TaggedID] :: kv._2)
      )
      .runCollect
      .flatMap(results => ZIO.collectAll(results).unit)
end MVStoreTagAssociationRepository

object MVStoreTagAssociationRepository:
  def apply[TagID: ToSegment, TaggedID: ToSegment, TaggedT](
      mvStore: MVStore,
      name: String
  ): MVStoreTagAssociationRepository[TagID, TaggedID, TaggedT] =
    new MVStoreTagAssociationRepository(
      SkZMVMap(mvStore.openMap[String, TaggedID](s"$name.tagged")),
      SkZMVMap(mvStore.openMap[String, TagID](s"$name.tag"))
    )

  def makeLayer[
      TagID: {zio.Tag, ToSegment},
      TaggedID: {zio.Tag, ToSegment},
      TaggedT: zio.Tag
  ](name: String): ZLayer[
    MVStore,
    Nothing,
    MVStoreTagAssociationRepository[TagID, TaggedID, TaggedT]
  ] =
    ZLayer.fromFunction(
      MVStoreTagAssociationRepository[TagID, TaggedID, TaggedT](
        _,
        name
      )
    )
