package solutions.s4y.vocabla.words.infra.kv.mvstore

import solutions.s4y.vocabla.domain.model.Identifier.identifier
import solutions.s4y.vocabla.words.app.repo.{EntryRepository, TagRepository}
import solutions.s4y.vocabla.words.domain.model.*
import solutions.s4y.vocabla.words.infra.kv.mvstore.Fixture.layerTestRepository
import zio.test.*
import zio.{Chunk, ZIO, ZLayer}

object MVStoreLiveTagRepositorySpect extends ZIOSpecDefault {
  def spec = suite("MVStoreRepositories")(
    suite("TagRepository")(
      test("add should create a new tag") {
        for {
          repository <- ZIO.service[TagRepository]
          tagId <- repository.add(Tag("test-tag", 1.identifier[Owner]))
          tags <- repository.get(1.identifier[Owner])
        } yield assertTrue(
          tagId == 11.identifier[Tag],
          tags.size == 1,
          tags.head.e.label == "test-tag",
          tags.head.id == 11.identifier[Tag]
        )
      },
      test("get should return all tags for specific owner") {
        for {
          repository <- ZIO.service[TagRepository]
          tagId1 <- repository.add(Tag("tag1", 1.identifier[Owner]))
          tagId2 <- repository.add(Tag("tag2", 1.identifier[Owner]))
          tagId3 <- repository.add(Tag("tag3", 2.identifier[Owner]))
          tags <- repository.get(1.identifier[Owner])
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
      test("get should return empty chunk when owner has no tags") {
        for {
          repository <- ZIO
            .service[TagRepository]
          tags <- repository.get(999.identifier[Owner])
        } yield assertTrue(
          tags.isEmpty
        )
      }
    ).provide(layerTestRepository),
    suite("EntryRepository")(
      test("add should create a new entry") {
        for {
          tagRepository <- ZIO.service[TagRepository]
          tagId1 <- tagRepository.add(Tag("C", 1))
          tagId2 <- tagRepository.add(Tag("A", 1))
          entryRepository <- ZIO
            .service[EntryRepository]
          entryId <- entryRepository.add(
            Entry(
              Headword("headword", "en"),
              Chunk(
                solutions.s4y.vocabla.words.domain.model.Definition(
                  "definition",
                  "es"
                )
              ),
              Chunk(tagId1, tagId2),
              1.identifier[Owner]
            )
          )
          entry <- entryRepository.get(1.identifier[Owner])
          tags <- tagRepository.get(1.identifier[Owner])
        } yield assertTrue(
          entryId == 13.identifier, // ID generation may vary
          entry.exists(_.e.headword.word == "headword"),
          entry.exists(_.e.headword.langCode == "en"),
          entry.head.e.definitions.size == 1,
          entry.head.e.definitions.head.definition == "definition",
          entry.head.e.definitions.head.langCode == "es",
          entry.head.e.tags.size == 2,
          entry.head.e.tags(1) == 12.identifier[Tag],
          tags.size == 2,
          tags(0).id == 11.identifier[Tag],
          tags(0).e == Tag("C", 1.identifier[Owner]),
          tags(1).id == 12.identifier[Tag],
          tags(1).e == Tag("A", 1.identifier[Owner])
        )
      }.provide(layerTestRepository),
      test("getEntry should return None for non-existent entry") {
        for {
          repository <- ZIO.service[EntryRepository]
          entry <- repository.get(999.identifier[Owner])
        } yield assertTrue(entry.isEmpty)
      }.provide(layerTestRepository),
      test("get should return all entries for specific owner") {
        for {
          repository <- ZIO.service[EntryRepository]
          entryId1 <- repository.add(
            Entry(
              Headword("word1", "en"),
              Chunk(Definition("def1", "es")),
              Chunk.empty,
              1
            )
          )
          entryId2 <- repository.add(
            Entry(
              Headword("word2", "en"),
              Chunk(Definition("def2", "es")),
              Chunk.empty,
              1
            )
          )
          entryId3 <- repository.add(
            Entry(
              Headword("word3", "en"),
              Chunk(Definition("def3", "es")),
              Chunk.empty,
              2
            )
          )
          entries <- repository.get(1.identifier[Owner])
        } yield assertTrue(
          entries.size == 2,
          entries.exists(e => e.id == entryId1 && e.e.headword.word == "word1"),
          entries.exists(e => e.id == entryId2 && e.e.headword.word == "word2"),
          entries.exists(e =>
            e.e.definitions.head.definition == "def1" && e.e.headword.langCode == "en"
          ),
          entries.exists(e =>
            e.e.definitions.head.definition == "def2" && e.e.headword.langCode == "en"
          )
        )
      }.provide(layerTestRepository)
    )
  )
}
