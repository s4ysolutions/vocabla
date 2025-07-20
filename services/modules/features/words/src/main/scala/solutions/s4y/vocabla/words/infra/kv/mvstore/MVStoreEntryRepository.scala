package solutions.s4y.vocabla.words.infra.kv.mvstore

import org.h2.mvstore.{MVMap, MVStore}
import solutions.s4y.vocabla.domain.model.{IdentifiedEntity, Identity}
import solutions.s4y.vocabla.id.IdFactory
import solutions.s4y.vocabla.lang.app.repo.LangRepository
import solutions.s4y.vocabla.lang.domain.model.Lang
import solutions.s4y.vocabla.words.app.repo.EntryRepository
import solutions.s4y.vocabla.words.domain.model.*
import solutions.s4y.vocabla.domain.model.Identity.IdConverter
import solutions.s4y.vocabla.words.domain.model.Tag.equalTag
import solutions.s4y.vocabla.words.infra.kv.mvstore.MVStoreEntryRepository.{MVStoreDefinition, MVStoreEntry}
import zio.{IO, Tag, ZIO, ZLayer}

class MVStoreEntryRepository[OwnerID: IdConverter, EntryID, TagID: IdConverter](
    map: MVMap[OwnerID, Seq[MVStoreEntry[EntryID, TagID]]],
    idFactory: IdFactory[EntryID],
    tagRepository: MVStoreTagRepository[OwnerID, TagID]
)(using langRepository: LangRepository)
    extends EntryRepository:

  override def addEntry(
      owner: Identity[Owner],
      word: String,
      wordLang: Lang.Code,
      definition: String,
      definitionLang: Lang.Code,
      tagLabels: Seq[String]
  ): IO[String, Identity[Entry]] = for {
    tags <- tagRepository.getTagsForOwner(owner)
    tagIds <- ZIO.foreachPar(tagLabels)(label =>
      tagRepository.addTag(owner, label)
    )
    entryId <- idFactory.next
    mvsEntry = MVStoreEntry(
      entryId,
      word,
      wordLang,
      List(MVStoreDefinition(definition, definitionLang)),
      tagIds.map(identity => identity.toId[TagID])
    )
    ownerId = owner.toId[OwnerID]
    mvsEntries <- getMVStoreEntriesForOwner(ownerId)
    _ <- ZIO
      .attempt(map.put(ownerId, mvsEntries :+ mvsEntry))
      .tapErrorCause(ZIO.logWarningCause(s"Error adding mvsEntry $mvsEntry", _))
      .mapError(th => s"Error adding mvsEntry $mvsEntry: ${th.getMessage}")
  } yield Identity(entryId)

  override def getEntriesForOwner(
      owner: Identity[Owner]
  ): IO[String, Seq[IdentifiedEntity[Entry]]] =
    ZIO
      .attempt(Option(map.get(owner.toId[OwnerID])).getOrElse(List.empty))
      .tapErrorCause(cause =>
        ZIO.logWarningCause(s"Error getting entries for owner $owner", cause)
      )
      .mapBoth(
        { th =>
          s"Error getting entries for owner $owner: ${th.getMessage}"
        },
        { mvsEntries =>
          mvsEntries.map { entry =>
            IdentifiedEntity(
              Identity(entry.id),
              Entry(
                Headword(entry.word, langRepository.getLang(entry.wordLang)),
                entry.definitions
                  .map(mvsDefn =>
                    Definition(
                      mvsDefn.definition,
                      langRepository.getLang(mvsDefn.lang)
                    )
                  ),
                entry.tags.map(tagId => Identity(tagId)),
                owner
              )
            )
          }
        }
      )

  private def getMVStoreEntriesForOwner(
      ownerId: OwnerID
  ): IO[String, Seq[MVStoreEntry[EntryID, TagID]]] =
    ZIO
      .attempt(Option(map.get(ownerId)).getOrElse(List.empty))
      .tapErrorCause(cause =>
        ZIO.logWarningCause(s"Error getting entries for owner $ownerId", cause)
      )
      .mapError(th =>
        s"Error getting entries for owner $ownerId: ${th.getMessage}"
      )

object MVStoreEntryRepository:
  case class MVStoreDefinition(
      definition: String,
      lang: Lang.Code
  )

  case class MVStoreEntry[EntryID, TagID](
      id: EntryID,
      word: String,
      wordLang: Lang.Code,
      definitions: List[MVStoreDefinition],
      tags: Seq[TagID]
  )

  def apply[OwnerID: IdConverter, EntryID, TagID: IdConverter](
      mvStore: MVStore,
      idFactory: IdFactory[EntryID],
      tagRepository: MVStoreTagRepository[OwnerID, TagID]
  )(using LangRepository): MVStoreEntryRepository[OwnerID, EntryID, TagID] =
    val map =
      mvStore.openMap[OwnerID, Seq[MVStoreEntry[EntryID, TagID]]]("entries")
    new MVStoreEntryRepository[OwnerID, EntryID, TagID](
      map,
      idFactory,
      tagRepository
    )

  def makeMvStoreLayer[
      OwnerID: {Tag, IdConverter},
      EntryID: Tag,
      TagID: {Tag, IdConverter}
  ](using
      LangRepository
  ): ZLayer[
    MVStore & MVStoreTagRepository[OwnerID, TagID] & IdFactory[EntryID],
    String,
    MVStoreEntryRepository[OwnerID, EntryID, TagID]
  ] =
    ZLayer.fromZIO {
      for {
        mvStore <- ZIO.service[MVStore]
        idFactory <- ZIO.service[IdFactory[EntryID]]
        tagRepository <- ZIO.service[MVStoreTagRepository[OwnerID, TagID]]
        entryRepository <- ZIO
          .attempt(
            MVStoreEntryRepository(mvStore, idFactory, tagRepository)
          )
          .tapError(th =>
            ZIO.logError(
              s"Error creating MVStoreEntryRepository: ${th.getMessage}"
            )
          )
          .mapError(th =>
            s"Error creating MVStoreEntryRepository: ${th.getMessage}"
          )
      } yield entryRepository
    }

  def makeLayer[OwnerID: Tag, EntryID: Tag, TagID: Tag]: ZLayer[
    MVStore & MVStoreEntryRepository[OwnerID, EntryID, TagID] &
      IdFactory[EntryID],
    String,
    EntryRepository
  ] = ZLayer.fromFunction(
    (repo: MVStoreEntryRepository[OwnerID, EntryID, TagID]) =>
      repo.asInstanceOf[
        EntryRepository
      ]
  )
