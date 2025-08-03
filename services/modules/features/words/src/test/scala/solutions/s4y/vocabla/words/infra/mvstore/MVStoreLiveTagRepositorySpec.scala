package solutions.s4y.vocabla.words.infra.mvstore

import solutions.s4y.vocabla.domain.model.Identified
import solutions.s4y.vocabla.domain.model.Identifier.identifier
import solutions.s4y.vocabla.words.app.repo.EntryRepository
import solutions.s4y.vocabla.words.domain.model.*
import solutions.s4y.vocabla.words.infra.mvstore.Fixture.layerTestRepository
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
      suite("Entry by owners")(
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
            Assertion.equalTo(Chunk(Identified(entryId, entry))))
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
            Assertion.equalTo(Chunk(Identified(entryId1, entry1), Identified(entryId2, entry2)))
          )
        },
        test ("add should not associate entry with a different owner") {
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
      )
    ).provide(layerTestRepository)
}
