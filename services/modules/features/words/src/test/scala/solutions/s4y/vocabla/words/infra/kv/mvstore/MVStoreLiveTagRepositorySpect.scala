package solutions.s4y.vocabla.words.infra.kv.mvstore

import solutions.s4y.vocabla.id.IdFactory
import solutions.s4y.vocabla.infrastructure.mvstore.KeyValueMVStore.makeMVStoreMemory
import solutions.s4y.vocabla.words.domain.model.Entity
import solutions.s4y.vocabla.words.infra.kv.mvstore.MVStoreLive.TagRepository
import solutions.s4y.vocabla.words.infra.kv.mvstore.MVStoreTagRepository.TagDTO
import zio.test.*
import zio.{Tag, UIO, ZIO, ZLayer}

import scala.language.postfixOps

object MVStoreLiveTagRepositorySpect extends ZIOSpecDefault {

  // Simple ID factory for testing that generates sequential IDs
  class TestIdFactory extends IdFactory[MVStoreLive.TagID] {
    private var currentId = 10
    override def next: UIO[MVStoreLive.TagID] = ZIO.succeed {
      currentId += 1
      currentId.asInstanceOf[MVStoreLive.TagID]
    }
  }

  def spec: Spec[Any, Serializable] = suite("MVStoreRepositories")(
    suite("TagRepository")(
      test("addTag should create a new tag") {
        for {
          repository <- ZIO.service[TagRepository]
          tagId <- repository.addTag(1L, "test-tag")
          tags <- repository.getTagsForOwner(1L)
        } yield assertTrue(
          tagId.long == 11L,
          tags.size == 1,
          tags.head.label == "test-tag",
          tags.head.id.long == 11L
        )
      },
      test("getTagsForOwner should return all tags for specific owner") {
        for {
          repository <- ZIO.service[TagRepository]
          tagId1 <- repository.addTag(1L, "tag1")
          tagId2 <- repository.addTag(1L, "tag2")
          tagId3 <- repository.addTag(2L, "tag3")
          tags <- repository.getTagsForOwner(1L)
        } yield assertTrue(
          tags.size == 2,
          tags.exists(t =>
            t.id.long == 11L && t.id == tagId1 && t.label == "tag1"
          ),
          tags.exists(t =>
            t.id.long == 12L && t.id == tagId2 && t.label == "tag2"
          ),
          !tags.exists(t => t.id == tagId3)
        )
      },
      test("getTagsForOwner should return empty chunk when owner has no tags") {
        for {
          repository <- ZIO.service[TagRepository]
          tags <- repository.getTagsForOwner(999L)
        } yield assertTrue(
          tags.isEmpty
        )
      }
    ),
    suite("EntryRepository")(
      test("addEntry should create a new entry") {
        for {
          tagRepository <- ZIO.service[MVStoreLive.TagRepository]
          _ <- tagRepository.addTag(1L, "C")
          _ <- tagRepository.addTag(1L, "A")
          entryRepository <- ZIO.service[MVStoreLive.EntryRepository]
          entryId <- entryRepository.addEntry(
            1L,
            "word",
            "en",
            "definition",
            "es",
            Seq("A", "B")
          )
          entry <- entryRepository.getEntriesForOwner(1L)
          tags <- tagRepository.getTagsForOwner(1L)
        } yield assertTrue(
          entryId.long == 14L,
          entry.exists(_.word == "word"),
          entry.exists(_.lang == "en"),
          entry.head.definitions.size == 1,
          entry.head.definitions.head.definition == "definition",
          entry.head.definitions.head.lang == "es",
          entry.head.tags.size == 2,
          entry.head.tags.head.long == 12L,
          entry.head.tags(1).long == 13L,
          tags.size == 3,
          tags.head == TagDTO(11L, "C"),
          tags(1) == TagDTO(12L, "A"),
          tags(2) == TagDTO(13L, "B")
        )
      },
      test("getEntry should return None for non-existent entry") {
        for {
          repository <- ZIO.service[MVStoreLive.EntryRepository]
          entry <- repository.getEntriesForOwner(999L)
        } yield assertTrue(entry.isEmpty)
      },
      test("getEntriesForOwner should return all entries for specific owner") {
        for {
          repository <- ZIO.service[MVStoreLive.EntryRepository]
          entryId1 <- repository.addEntry(
            1L,
            "word1",
            "en",
            "def1",
            "es",
            Seq.empty
          )
          entryId2 <- repository.addEntry(
            1L,
            "word2",
            "en",
            "def2",
            "es",
            Seq.empty
          )
          _ <- repository.addEntry(2L, "word3", "en", "def3", "es", Seq.empty)
          entries <- repository.getEntriesForOwner(1L)
        } yield assertTrue(
          entries.size == 2,
          entries.exists(e => e.id == entryId1 && e.word == "word1"),
          entries.exists(e => e.id == entryId2 && e.word == "word2"),
          entries.exists(e =>
            e.definitions.head.definition == "def1" && e.lang == "en"
          ),
          entries.exists(e =>
            e.definitions.head.definition == "def2" && e.lang == "en"
          )
        )
      }
    )
  ).provide(
    (ZLayer.scoped(makeMVStoreMemory()) ++ ZLayer.succeed(
      new TestIdFactory
    )) >>> MVStoreLive.layers
  )
}
