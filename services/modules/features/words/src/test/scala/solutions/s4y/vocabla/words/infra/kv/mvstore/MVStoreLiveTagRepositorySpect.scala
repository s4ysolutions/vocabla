package solutions.s4y.vocabla.words.infra.kv.mvstore

import solutions.s4y.vocabla.words.app.repo.dto.{EntryDTO, TagDTO}
import solutions.s4y.vocabla.words.app.repo.{EntryRepository, TagRepository}
import solutions.s4y.vocabla.words.infra.kv.mvstore.Fixture.{
  ID,
  layerEntryRepository,
  layerTagRepository
}
import zio.test.*
import zio.{Tag, ZIO, ZLayer}

import scala.language.postfixOps

object MVStoreLiveTagRepositorySpect extends ZIOSpecDefault {
  def spec = suite("MVStoreRepositories")(
    suite("TagRepository")(
      test("addTag should create a new tag") {
        for {
          repository <- ZIO.service[TagRepository[ID, ID, TagDTO[ID]]]
          tagId <- repository.addTag(1, "test-tag")
          tags <- repository.getTagsForOwner(1)
        } yield assertTrue(
          tagId == 11,
          tags.size == 1,
          tags.head.label == "test-tag",
          tags.head.id == 11
        )
      },
      test("getTagsForOwner should return all tags for specific owner") {
        for {
          repository <- ZIO.service[TagRepository[ID, ID, TagDTO[ID]]]
          tagId1 <- repository.addTag(1, "tag1")
          tagId2 <- repository.addTag(1, "tag2")
          tagId3 <- repository.addTag(2, "tag3")
          tags <- repository.getTagsForOwner(1)
        } yield assertTrue(
          tags.size == 2,
          tags.exists(t => t.id == 11 && t.id == tagId1 && t.label == "tag1"),
          tags.exists(t => t.id == 12 && t.id == tagId2 && t.label == "tag2"),
          !tags.exists(t => t.id == tagId3)
        )
      },
      test("getTagsForOwner should return empty chunk when owner has no tags") {
        for {
          repository <- ZIO
            .service[TagRepository[ID, ID, TagDTO[ID]]]
          tags <- repository.getTagsForOwner(999)
        } yield assertTrue(
          tags.isEmpty
        )
      }
    ).provide(layerTagRepository),
    suite("EntryRepository")(
      test("addEntry should create a new entry") {
        for {
          tagRepository <- ZIO.service[TagRepository[ID, ID, TagDTO[ID]]]
          _ <- tagRepository.addTag(1, "C")
          _ <- tagRepository.addTag(1, "A")
          entryRepository <- ZIO
            .service[EntryRepository[ID, ID, EntryDTO[ID, ID]]]
          entryId <- entryRepository.addEntry(
            1,
            "word",
            "en",
            "definition",
            "es",
            Seq("A", "B")
          )
          entry <- entryRepository.getEntriesForOwner(1)
          tags <- tagRepository.getTagsForOwner(1)
        } yield assertTrue(
          entryId == 14,
          entry.exists(_.word == "word"),
          entry.exists(_.lang == "en"),
          entry.head.definitions.size == 1,
          entry.head.definitions.head.definition == "definition",
          entry.head.definitions.head.lang == "es",
          entry.head.tags.size == 2,
          entry.head.tags.head == 12,
          entry.head.tags(1) == 13,
          tags.size == 3,
          tags.head == TagDTO(11, "C"),
          tags(1) == TagDTO(12, "A"),
          tags(2) == TagDTO(13, "B")
        )
      }.provide(layerEntryRepository ++ layerTagRepository),
      test("getEntry should return None for non-existent entry") {
        for {
          repository <- ZIO.service[EntryRepository[ID, ID, EntryDTO[ID, ID]]]
          entry <- repository.getEntriesForOwner(999)
        } yield assertTrue(entry.isEmpty)
      }.provide(layerEntryRepository ++ layerTagRepository),
      test("getEntriesForOwner should return all entries for specific owner") {
        for {
          repository <- ZIO.service[EntryRepository[ID, ID, EntryDTO[ID, ID]]]
          entryId1 <- repository.addEntry(
            1,
            "word1",
            "en",
            "def1",
            "es",
            Seq.empty
          )
          entryId2 <- repository.addEntry(
            1,
            "word2",
            "en",
            "def2",
            "es",
            Seq.empty
          )
          _ <- repository.addEntry(2, "word3", "en", "def3", "es", Seq.empty)
          entries <- repository.getEntriesForOwner(1)
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
      }.provide(layerEntryRepository ++ layerTagRepository)
    )
  )
}
