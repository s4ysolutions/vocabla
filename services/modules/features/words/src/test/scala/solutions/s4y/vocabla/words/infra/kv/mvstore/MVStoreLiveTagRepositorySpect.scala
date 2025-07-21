package solutions.s4y.vocabla.words.infra.kv.mvstore

import solutions.s4y.vocabla.domain.model.Identifier
import solutions.s4y.vocabla.domain.model.Identifier.identifier
import solutions.s4y.vocabla.words.app.repo.{EntryRepository, TagRepository}
import solutions.s4y.vocabla.words.domain.model.{Owner, Tag}
import solutions.s4y.vocabla.words.infra.kv.mvstore.Fixture.{ID, layerTestRepository, given}
import zio.prelude.EqualOps
import zio.test.*
import zio.{Chunk, ZIO, ZLayer}

import scala.language.postfixOps

object MVStoreLiveTagRepositorySpect extends ZIOSpecDefault {
  def spec = suite("MVStoreRepositories")(
    suite("TagRepository")(
      test("addTag should create a new tag") {
        for {
          repository <- ZIO.service[TagRepository]
          tagId <- repository.addTag(1.identifier[Owner], "test-tag")
          tags <- repository.getTagsForOwner(1.identifier[Owner])
        } yield assertTrue(
          tagId == 11.identifier[Tag],
          tags.size == 1,
          tags.head.e.label == "test-tag",
          tags.head.id == 11.identifier[Tag]
        )
      },
      test("getTagsForOwner should return all tags for specific owner") {
        for {
          repository <- ZIO.service[TagRepository]
          tagId1 <- repository.addTag(1.identifier[Owner], "tag1")
          tagId2 <- repository.addTag(1.identifier[Owner], "tag2")
          tagId3 <- repository.addTag(2.identifier[Owner], "tag3")
          tags <- repository.getTagsForOwner(1.identifier[Owner])
        } yield assertTrue(
          tags.size == 2,
          tags.exists(t =>
            t.id == 11
              .identifier[
                Tag
              ] && t.id == tagId1 && t.e.label == "tag1"
          ),
          tags.exists(t =>
            t.id == 12.identifier && t.id == tagId2 && t.e.label == "tag2"
          ),
          !tags.exists(t => t.id == tagId3)
        )
      },
      test("getTagsForOwner should return empty chunk when owner has no tags") {
        for {
          repository <- ZIO
            .service[TagRepository]
          tags <- repository.getTagsForOwner(999.identifier[Owner])
        } yield assertTrue(
          tags.isEmpty
        )
      }
    ).provide(layerTestRepository),
    suite("EntryRepository")(
      test("addEntry should create a new entry") {
        for {
          tagRepository <- ZIO.service[TagRepository]
          _ <- tagRepository.addTag(1.identifier[Owner], "C")
          _ <- tagRepository.addTag(1.identifier[Owner], "A")
          entryRepository <- ZIO
            .service[EntryRepository]
          entryId <- entryRepository.addEntry(
            1.identifier[Owner],
            "headword",
            "en",
            "definition",
            "es",
            Chunk("A", "B")
          )
          entry <- entryRepository.getEntriesForOwner(1.identifier[Owner])
          tags <- tagRepository.getTagsForOwner(1.identifier[Owner])
        } yield assertTrue(
          entryId == 14.identifier, // ID generation may vary
          entry.exists(_.e.headword.word == "headword"),
          entry.exists(_.e.headword.lang.code == "en"),
          entry.head.e.definitions.size == 1,
          entry.head.e.definitions.head.definition == "definition",
          entry.head.e.definitions.head.lang.code == "es",
          entry.head.e.tags.size == 2,
          entry.head.e.tags.head == 12.identifier[Tag],
          entry.head.e.tags(1) == 13.identifier[Tag],
          tags.size == 3,
          tags.head.id == 11.identifier[Tag],
          tags.head.e == Tag("C", 1.identifier[Owner]),
          tags(1).id == 12.identifier[Tag],
          tags(1).e == Tag("A", 1.identifier[Owner]),
          tags(2).id == 13.identifier[Tag],
          tags(2).e == Tag("B", 1.identifier[Owner])
        )
      }.provide(layerTestRepository),
      test("getEntry should return None for non-existent entry") {
        for {
          repository <- ZIO.service[EntryRepository]
          entry <- repository.getEntriesForOwner(999.identifier[Owner])
        } yield assertTrue(entry.isEmpty)
      }.provide(layerTestRepository),
      test("getEntriesForOwner should return all entries for specific owner") {
        for {
          repository <- ZIO.service[EntryRepository]
          entryId1 <- repository.addEntry(
            1.identifier[Owner],
            "word1",
            "en",
            "def1",
            "es",
            Chunk.empty
          )
          entryId2 <- repository.addEntry(
            1.identifier[Owner],
            "word2",
            "en",
            "def2",
            "es",
            Chunk.empty
          )
          _ <- repository.addEntry(
            2.identifier[Owner],
            "word3",
            "en",
            "def3",
            "es",
            Chunk.empty
          )
          entries <- repository.getEntriesForOwner(1.identifier[Owner])
        } yield assertTrue(
          entries.size == 2,
          entries.exists(e =>
            e.id == entryId1 && e.e.headword.word == "word1"
          ),
          entries.exists(e =>
            e.id == entryId2 && e.e.headword.word == "word2"
          ),
          entries.exists(e =>
            e.e.definitions.head.definition == "def1" && e.e.headword.lang.code == "en"
          ),
          entries.exists(e =>
            e.e.definitions.head.definition == "def2" && e.e.headword.lang.code == "en"
          )
        )
      }.provide(layerTestRepository)
    )
  )
}
