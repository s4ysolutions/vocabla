package solutions.s4y.vocabla.words.infra.kv.mvstore

import org.h2.mvstore.MVStore
import solutions.s4y.vocabla.id.IdFactory
import solutions.s4y.vocabla.words.domain.model.{Entry, Owner, Tag}
import zio.{ZIO, ZLayer}

/** Terminates polymorphism by providing the concrete implementation and define
  * non-parameterized type
  */
object MVStoreLive:
  private type EntryID = Entry.Id
  private type OwnerID = Owner.Id
  private type TagID = Tag.Id
  private type EntryDTO = MVStoreEntryRepository.EntryDTO[EntryID, TagID]
  private type TagDTO = MVStoreTagRepository.TagDTO[TagID]
  private[mvstore] type TagRepository =
    solutions.s4y.vocabla.words.app.repo.TagRepository[OwnerID, TagID, TagDTO]
  private[mvstore] type EntryRepository =
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
    TagRepository
    // MVStoreTagRepository[OwnerID, TagID]
  ] = for {
    mvStore <- ZIO.service[MVStore]
    tagIdFactory <- ZIO.service[IdFactory[TagID]]
    repository <- MVStoreTagRepository[OwnerID, TagID](mvStore, tagIdFactory)
  } yield repository

  private val entriesLive: ZIO[
    TagRepository & MVStore & IdFactory[EntryID],
    String,
    EntryRepository
  ] =
    for {
      mvStore <- ZIO.service[MVStore]
      tagRepository <- ZIO.service[TagRepository]
      entryIdFactory <- ZIO.service[IdFactory[EntryID]]
      entryRepository <- MVStoreEntryRepository(
        mvStore,
        entryIdFactory,
        // hack!
        tagRepository.asInstanceOf[MVStoreTagRepository[OwnerID, TagID]]
      )
    } yield entryRepository

  val layers: ZLayer[
    MVStore & IdFactory[TagID] & IdFactory[EntryID],
    String,
    TagRepository & EntryRepository
  ] = {
    val tagsLayer: ZLayer[MVStore & IdFactory[TagID], String, TagRepository] =
      ZLayer.fromZIO(tagsLive)

    val entriesLayer: ZLayer[
      TagRepository & MVStore & IdFactory[EntryID],
      String,
      EntryRepository
    ] = ZLayer.fromZIO(entriesLive)

    tagsLayer ++ (tagsLayer >>> entriesLayer)
  }
