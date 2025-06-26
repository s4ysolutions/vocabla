package solutions.s4y.vocabla.words.infra.kv.mvstore

import org.h2.mvstore.MVStore
import solutions.s4y.vocabla.id.IdFactory
import solutions.s4y.vocabla.words.domain.model.{Entry, Owner, Tag}
import zio.{ZIO, ZLayer}

object MVStoreLive:
  type EntryID = Entry.Id
  type OwnerID = Owner.Id
  type TagID = Tag.Id
  type EntryDTO = MVStoreEntryRepository.EntryDTO[EntryID, TagID]
  type TagDTO = MVStoreTagRepository.TagDTO[TagID]
  type TagRepository =
    solutions.s4y.vocabla.words.app.repo.TagRepository[OwnerID, TagID, TagDTO]
  type EntryRepository =
    solutions.s4y.vocabla.words.app.repo.EntryRepository[
      OwnerID,
      EntryID,
      EntryDTO
    ]

  // noinspection ScalaRedundantCast
  val liveTagIdFactory: IdFactory[TagID] =
    IdFactory.long.asInstanceOf[IdFactory[TagID]]
  // noinspection ScalaRedundantCast
  val liveEntryIdFactory: IdFactory[EntryID] =
    IdFactory.long.asInstanceOf[IdFactory[EntryID]]

  private val tagsLive: ZIO[
    MVStore & IdFactory[TagID],
    String,
    MVStoreTagRepository[OwnerID, TagID]
  ] = for {
    mvStore <- ZIO.service[MVStore]
    tagIdFactory <- ZIO.service[IdFactory[TagID]]
    repository <- MVStoreTagRepository[OwnerID, TagID](mvStore, tagIdFactory)
  } yield repository

  private val entriesLive: ZIO[
    MVStoreTagRepository[OwnerID, TagID] & MVStore & IdFactory[EntryID],
    String,
    MVStoreEntryRepository[OwnerID, EntryID, TagID]
  ] =
    for {
      mvStore <- ZIO.service[MVStore]
      // tagRepository <- ZIO.service[MVStoreTagRepository[OwnerID, TagID]]
      // tagRepository <- ZIO.service[TagRepository]
      tagRepository <- tagsLive
      entryIdFactory <- ZIO.service[IdFactory[EntryID]]
      entryRepository <- MVStoreEntryRepository(
        mvStore,
        entryIdFactory,
        tagRepository
      )
    } yield entryRepository

  val layers: ZLayer[
    MVStore,
    String,
    TagRepository & EntryRepository
  ] = {
    val tagsLayer = ZLayer.fromZIO(
      tagsLive
        .asInstanceOf[
          ZIO[MVStore, String, TagRepository]
        ]
    )

    val entriesLayer = ZLayer.fromZIO(
      entriesLive
        .asInstanceOf[
          ZIO[MVStore, String, EntryRepository]
        ]
    )

    tagsLayer ++ entriesLayer
  }
