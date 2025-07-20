package solutions.s4y.vocabla.words.infra.kv.mvstore

import solutions.s4y.vocabla.domain.model.Identifier
import solutions.s4y.vocabla.domain.model.Identifier.identity
import solutions.s4y.vocabla.words.app.repo.{EntryRepository, TagRepository}
import solutions.s4y.vocabla.words.domain.model.{Owner, Tag}
import solutions.s4y.vocabla.words.infra.kv.mvstore.Fixture.{
  ID,
  layerTestRepository,
  given
}
import zio.prelude.EqualOps
import zio.test.*
import zio.{ZIO, ZLayer}

import scala.language.postfixOps

object MVStoreLiveTagRepositorySpect extends ZIOSpecDefault {
  def spec = suite("MVStoreRepositories")(
    suite("TagRepository")(
      test("addTag should create a new tag") {
        for {
          repository <- ZIO.service[TagRepository]
          tagId <- repository.addTag(1.identity[Owner], "test-tag")
          tags <- repository.getTagsForOwner(1.identity[Owner])
        } yield assertTrue(
          tagId == 11.identity[Tag],
          tags.size == 1,
          tags.head.e.label == "test-tag",
          tags.head.id == 11.identity[Tag]
        )
      },
      test("getTagsForOwner should return all tags for specific owner") {
        for {
          repository <- ZIO.service[TagRepository]
          tagId1 <- repository.addTag(1.identity[Owner], "tag1")
          tagId2 <- repository.addTag(1.identity[Owner], "tag2")
          tagId3 <- repository.addTag(2.identity[Owner], "tag3")
          tags <- repository.getTagsForOwner(1.identity[Owner])
        } yield assertTrue(
          tags.size == 2,
          tags.exists(t =>
            t.id == 11
              .identity[
                Tag
              ] && t.id == tagId1 && t.e.label == "tag1"
          ),
          tags.exists(t =>
            t.id == 12.identity && t.id == tagId2 && t.e.label == "tag2"
          ),
          !tags.exists(t => t.id == tagId3)
        )
      },
      test("getTagsForOwner should return empty chunk when owner has no tags") {
        for {
          repository <- ZIO
            .service[TagRepository]
          tags <- repository.getTagsForOwner(999.identity[Owner])
        } yield assertTrue(
          tags.isEmpty
        )
      }
    ).provide(layerTestRepository),
    suite("EntryRepository")(
      test("addEntry should create a new entry") {
        for {
          tagRepository <- ZIO.service[TagRepository]
          _ <- tagRepository.addTag(1.identity[Owner], "C")
          _ <- tagRepository.addTag(1.identity[Owner], "A")
          entryRepository <- ZIO
            .service[EntryRepository]
          entryId <- entryRepository.addEntry(
            1.identity[Owner],
            "headword",
            "en",
            "definition",
            "es",
            Seq("A", "B")
          )
          entry <- entryRepository.getEntriesForOwner(1.identity[Owner])
          tags <- tagRepository.getTagsForOwner(1.identity[Owner])
        } yield assertTrue(
          entryId == 14.identity, // ID generation may vary
          entry.exists(_.e.headword.word == "headword"),
          entry.exists(_.e.headword.lang.code == "en"),
          entry.head.e.definitions.size == 1,
          entry.head.e.definitions.head.definition == "definition",
          entry.head.e.definitions.head.lang.code == "es",
          entry.head.e.tags.size == 2,
          entry.head.e.tags.head == 12.identity[Tag],
          entry.head.e.tags(1) == 13.identity[Tag],
          tags.size == 3,
          tags.head.id == 11.identity[Tag],
          tags.head.e == Tag("C", 1.identity[Owner]),
          tags(1).id == 12.identity[Tag],
          tags(1).e == Tag("A", 1.identity[Owner]),
          tags(2).id == 13.identity[Tag],
          tags(2).e == Tag("B", 1.identity[Owner])
        )
      }.provide(layerTestRepository),
      test("getEntry should return None for non-existent entry") {
        for {
          repository <- ZIO.service[EntryRepository]
          entry <- repository.getEntriesForOwner(999.identity[Owner])
        } yield assertTrue(entry.isEmpty)
      }.provide(layerTestRepository),
      test("getEntriesForOwner should return all entries for specific owner") {
        for {
          repository <- ZIO.service[EntryRepository]
          entryId1 <- repository.addEntry(
            1.identity[Owner],
            "word1",
            "en",
            "def1",
            "es",
            Seq.empty
          )
          entryId2 <- repository.addEntry(
            1.identity[Owner],
            "word2",
            "en",
            "def2",
            "es",
            Seq.empty
          )
          _ <- repository.addEntry(
            2.identity[Owner],
            "word3",
            "en",
            "def3",
            "es",
            Seq.empty
          )
          entries <- repository.getEntriesForOwner(1.identity[Owner])
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
