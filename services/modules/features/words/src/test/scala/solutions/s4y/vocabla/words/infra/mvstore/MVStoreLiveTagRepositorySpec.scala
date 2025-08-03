package solutions.s4y.vocabla.words.infra.mvstore

import solutions.s4y.vocabla.domain.model.Identified
import solutions.s4y.vocabla.domain.model.Identifier.identifier
import solutions.s4y.vocabla.tags.domain.Tag
import solutions.s4y.vocabla.words.app.repo.EntryRepository
import solutions.s4y.vocabla.words.domain.model.*
import solutions.s4y.vocabla.words.infra.mvstore.Fixture.layerTestRepository
import solutions.s4y.vocabla.words.infra.mvstore.MVStoreLiveTagRepositorySpec.test
import zio.test.*
import zio.{Chunk, Scope, ZIO, ZLayer}

object MVStoreLiveTagRepositorySpec extends ZIOSpecDefault {
  def spec: Spec[TestEnvironment & Scope, Any] =
    suite("MVStoreEntryRepository")(
      suite("Entry")(
        test("add should create a new entry") {
          val entry = Entry(
            Headword("headword", "en"),
            Chunk(Definition("definition", "es")),
            Chunk.empty
          )
          val ownerId = 1.identifier[Owner]
          for {
            repo <- ZIO.service[EntryRepository]
            entryId <- repo.put(ownerId, entry)
            retrievedEntry <- repo.get(entryId)
          } yield assert(retrievedEntry)(Assertion.equalTo(Some(entry)))
        }
      ),
      suite("Entry with owners")(
        test("add should associate entry with owner") {
          val entry = Entry(
            Headword("headword", "en"),
            Chunk(Definition("definition", "es")),
            Chunk.empty
          )
          val ownerId = 1.identifier[Owner]
          for {
            repo <- ZIO.service[EntryRepository]
            entryId <- repo.put(ownerId, entry)
            entries <- repo.getForOwner(ownerId).runCollect
          } yield assert(entries)(
            Assertion.equalTo(Chunk(Identified(entryId, entry)))
          )
        },
        test("add should associate multiple entries with the same owner") {
          val entry1 = Entry(
            Headword("headword1", "en"),
            Chunk(Definition("definition1", "es")),
            Chunk.empty
          )
          val entry2 = Entry(
            Headword("headword2", "en"),
            Chunk(Definition("definition2", "es")),
            Chunk.empty
          )
          val ownerId = 1.identifier[Owner]
          for {
            repo <- ZIO.service[EntryRepository]
            entryId1 <- repo.put(ownerId, entry1)
            entryId2 <- repo.put(ownerId, entry2)
            entries <- repo.getForOwner(ownerId).runCollect
          } yield assert(entries)(
            Assertion.equalTo(
              Chunk(Identified(entryId1, entry1), Identified(entryId2, entry2))
            )
          )
        },
        test("add should not associate entry with a different owner") {
          val entry1 = Entry(
            Headword("headword", "en"),
            Chunk(Definition("definition", "es")),
            Chunk.empty
          )
          val entry2 = Entry(
            Headword("headword2", "en"),
            Chunk(Definition("definition2", "es")),
            Chunk.empty
          )
          val ownerId1 = 1.identifier[Owner]
          val ownerId2 = 2.identifier[Owner]
          for {
            repo <- ZIO.service[EntryRepository]
            entryId1 <- repo.put(ownerId1, entry1)
            entryId2 <- repo.put(ownerId2, entry2)
            entries <- repo.getForOwner(ownerId1).runCollect
          } yield assert(entries)(
            Assertion.equalTo(Chunk(Identified(entryId1, entry1)))
          )
        }
      ),
      suite("Entry with tags")(
        test("add should associate entry with tag") {
          val entry = Entry(
            Headword("headword", "en"),
            Chunk(Definition("definition", "es")),
            Chunk.empty
          )
          val tagId = 1.identifier[Tag]
          val entryExpected = Entry(
            Headword("headword", "en"),
            Chunk(Definition("definition", "es")),
            Chunk(tagId)
          )
          for {
            repo <- ZIO.service[EntryRepository]
            entryId <- repo.put(1.identifier[Owner], entry)
            _ <- repo.addTag(entryId, tagId)
            entries <- repo.getForTag(tagId).runCollect
          } yield assert(entries)(
            Assertion.equalTo(Chunk(Identified(entryId, entryExpected)))
          )
        },
        test("add should associate multiple entries with the same tag") {
          val entry1 = Entry(
            Headword("headword1", "en"),
            Chunk(Definition("definition1", "es")),
            Chunk.empty
          )
          val entry2 = Entry(
            Headword("headword2", "en"),
            Chunk(Definition("definition2", "es")),
            Chunk.empty
          )
          val tagId = 1.identifier[Tag]
          val entry1Expected = Entry(
            Headword("headword1", "en"),
            Chunk(Definition("definition1", "es")),
            Chunk(tagId)
          )
          val entry2Expected = Entry(
            Headword("headword2", "en"),
            Chunk(Definition("definition2", "es")),
            Chunk(tagId)
          )
          for {
            repo <- ZIO.service[EntryRepository]
            entryId1 <- repo.put(1.identifier[Owner], entry1)
            entryId2 <- repo.put(1.identifier[Owner], entry2)
            _ <- repo.addTag(entryId1, tagId)
            _ <- repo.addTag(entryId2, tagId)
            entries <- repo.getForTag(tagId).runCollect
          } yield assert(entries)(
            Assertion.equalTo(
              Chunk(
                Identified(entryId1, entry1Expected),
                Identified(entryId2, entry2Expected)
              )
            )
          )
        },
        test("removeTag should remove association with tag") {
          val entry = Entry(
            Headword("headword", "en"),
            Chunk(Definition("definition", "es")),
            Chunk.empty
          )
          val tagId = 1.identifier[Tag]
          for {
            repo <- ZIO.service[EntryRepository]
            entryId <- repo.put(1.identifier[Owner], entry)
            _ <- repo.addTag(entryId, tagId)
            _ <- repo.removeTag(entryId, tagId)
            entries <- repo.getForTag(tagId).runCollect
          } yield assert(entries)(Assertion.isEmpty)
        },
        test("removeTag should remove association with tag but leave another tag intact") {
          val entry1 = Entry(
            Headword("headword", "en"),
            Chunk(Definition("definition", "es")),
            Chunk.empty
          )
          val entry2 = Entry(
            Headword("headword2", "en"),
            Chunk(Definition("definition2", "es")),
            Chunk.empty
          )
          val tagId1 = 1.identifier[Tag]
          val tagId2 = 2.identifier[Tag]
          val entry1withTag1 = Entry(
            Headword("headword", "en"),
            Chunk(Definition("definition", "es")),
            Chunk(tagId1)
          )
          val entry2withTag2 = Entry(
            Headword("headword2", "en"),
            Chunk(Definition("definition2", "es")),
            Chunk(tagId2)
          )
          val entry2withTags12 = Entry(
            Headword("headword2", "en"),
            Chunk(Definition("definition2", "es")),
            Chunk(tagId1, tagId2)
          )
          for {
            repo <- ZIO.service[EntryRepository]
            entryId1 <- repo.put(1.identifier[Owner], entry1)
            entryId2 <- repo.put(1.identifier[Owner], entry2)
            _ <- repo.addTag(entryId1, tagId1)
            _ <- repo.addTag(entryId2, tagId1)
            _ <- repo.addTag(entryId2, tagId2)
            entriesForTag1 <- repo.getForTag(tagId1).runCollect
            entriesForTag2 <- repo.getForTag(tagId2).runCollect
            _ <- repo.removeTag(entryId2, tagId1)
            entriesForTag1a <- repo.getForTag(tagId1).runCollect
            entriesForTag2a <- repo.getForTag(tagId2).runCollect
          } yield assert(entriesForTag1)(
            Assertion.equalTo(Chunk(Identified(entryId1, entry1withTag1), Identified(entryId2, entry2withTags12)))
          ) && assert(entriesForTag2)(
            Assertion.equalTo(Chunk(Identified(entryId2, entry2withTags12)))
          ) && assert(entriesForTag1a)(
            Assertion.equalTo(Chunk(Identified(entryId1, entry1withTag1)))
          ) && assert(entriesForTag2a)(
            Assertion.equalTo(Chunk(Identified(entryId2, entry2withTag2)))
          )
        }
      )
    ).provide(layerTestRepository)
}
