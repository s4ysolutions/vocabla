package solutions.s4y.vocabla.infra.pgsql

import solutions.s4y.vocabla.app.repo.EntryRepository
import solutions.s4y.vocabla.app.repo.tx.TransactionManager
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.{Definition, Entry, Headword, Student, Tag}
import solutions.s4y.zio.consoleColorDebugLogger
import zio.{Chunk, Scope, ZIO}
import zio.test.{Spec, TestAspect, TestEnvironment, ZIOSpecDefault, assert}

object EntryRepositoryPgSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("EntryRepositoryPgSpec")(
    suite("Create")(
      test("create an entry") {
        for {
          transactionManager <- ZIO.service[TransactionManager]
          id <- transactionManager.transaction {
            for {
              repo <- ZIO.service[EntryRepository]
              entry = Entry(
                Headword("Test Entry", "en"),
                Chunk(Definition("Test Definition", "en")),
                1L.identifier[Student]
              )
              id <- repo.create(entry)
            } yield id
          }
        } yield assert(id)(zio.test.Assertion.equalTo(1.identifier[Entry]))
      },
      test("get an entry") {
        for {
          transactionManager <- ZIO.service[TransactionManager]
          entryOpt <- transactionManager.transaction {
            for {
              repo <- ZIO.service[EntryRepository]
              entry = Entry(
                Headword("Test Entry", "en"),
                Chunk(Definition("Test Definition", "en")),
                1L.identifier[Student]
              )
              id <- repo.create(entry)
              entryOpt <- repo.get(id)
            } yield entryOpt
          }
        } yield assert(entryOpt.get)(
          zio.test.Assertion.equalTo(
            Entry(
              Headword("Test Entry", "en"),
              Chunk(Definition("Test Definition", "en")),
              1L.identifier[Student]
            )
          )
        )
      },
      test("delete an entry") {
        for {
          transactionManager <- ZIO.service[TransactionManager]
          deleted <- transactionManager.transaction {
            for {
              repo <- ZIO.service[EntryRepository]
              entry = Entry(
                Headword("Test Entry", "en"),
                Chunk(Definition("Test Definition", "en")),
                1L.identifier[Student]
              )
              id <- repo.create(entry)
              deleted <- repo.delete(id)
            } yield deleted
          }
        } yield assert(deleted)(zio.test.Assertion.isTrue)
      }
    )
  ).provideLayer(
    consoleColorDebugLogger >>> Fixture.layerWithEntryRepository
  ) @@ TestAspect.before(Fixture.testSystem) @@ TestAspect.sequential
}
