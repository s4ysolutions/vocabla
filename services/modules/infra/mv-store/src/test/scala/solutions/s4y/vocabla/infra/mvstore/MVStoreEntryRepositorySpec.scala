package solutions.s4y.vocabla.infra.mvstore

import solutions.s4y.vocabla.app.repo.EntryRepository
import solutions.s4y.vocabla.domain.entry.{Definition, Headword}
import solutions.s4y.vocabla.domain.identity.Identified
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.{Entry, Student}
import solutions.s4y.vocabla.infra.mvstore.Fixture.makeEntryRepositoryLayer
import zio.test.*
import zio.test.Assertion.{equalTo, isNone, isSome, isTrue}
import zio.{Chunk, Scope, ZIO}

object MVStoreEntryRepositorySpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment & Scope, Any] = suite("MVStoreEntryRepositorySpec")(
    test("create should add and get an entry") {
      val ownerId = 1.identifier[Student]
      val entry = Entry(
        headword = Headword("word1", "en"),
        definitions = Chunk(Definition("def1", "en")),
        ownerId = ownerId
      )
      for {
        repo <- ZIO.service[EntryRepository]
        createdId <- repo.createEntry(entry)
        fetched <- repo.readEntry(createdId)
      } yield assert(fetched)(isSome(equalTo(entry)))
    },
    test("update should modify an existing entry") {
      val ownerId = 2.identifier[Student]
      val entry = Entry(
        headword = Headword("word2", "en"),
        definitions = Chunk(Definition("def2", "en")),
        ownerId = ownerId
      )
      for {
        repo <- ZIO.service[EntryRepository]
        createdId <- repo.createEntry(entry)
        updated <- repo.updateEntry(Identified(createdId, entry.copy(headword = Headword("word2-updated", "en"))))
        fetched <- repo.readEntry(createdId)
      } yield assert(updated)(isTrue) && assert(fetched)(isSome(equalTo(entry.copy(headword = Headword("word2-updated", "en")))))
    },
    test("delete should remove an entry") {
      val ownerId = 3.identifier[Student]
      val entry = Entry(
        headword = Headword("word3", "en"),
        definitions = Chunk(Definition("def3", "en")),
        ownerId = ownerId
      )
      for {
        repo <- ZIO.service[EntryRepository]
        createdId <- repo.createEntry(entry)
        deleted <- repo.deleteEntry(createdId)
        fetched <- repo.readEntry(createdId)
      } yield assert(deleted)(isTrue) && assert(fetched)(isNone)
    },
    test("readEntry should return None for non-existent entry") {
      val nonExistentId = 999.identifier[Entry]
      for {
        repo <- ZIO.service[EntryRepository]
        fetched <- repo.readEntry(nonExistentId)
      } yield assert(fetched)(isNone)
    }
  ).provideLayer(makeEntryRepositoryLayer())
}

