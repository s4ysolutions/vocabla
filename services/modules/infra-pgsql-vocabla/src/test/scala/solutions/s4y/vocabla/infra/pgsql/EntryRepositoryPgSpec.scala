package solutions.s4y.vocabla.infra.pgsql

import solutions.s4y.infra.pgsql.tx.TransactionManagerPg
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.{Entry, Tag, User}
import solutions.s4y.zio.{consoleColorDebugLogger, consoleColorTraceLogger}
import zio.{Chunk, Scope, ZIO}
import zio.test.{Spec, TestAspect, TestEnvironment, ZIOSpecDefault, assert}

object EntryRepositoryPgSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("EntryRepositoryPgSpec")(
      suite("Create")(
        test("create an entry") {
          for {
            transactionManager <- ZIO.service[TransactionManagerPg]
            id <- transactionManager.transaction {
              for {
                repo <- ZIO.service[EntryRepositoryPg]
                entry = Entry(
                  Entry.Headword("Test Entry", "en"),
                  Chunk(Entry.Definition("Test Definition", "en")),
                  1L.identifier[User.Student]
                )
                id <- repo.create(entry)
              } yield id
            }
          } yield assert(id)(zio.test.Assertion.equalTo(1.identifier[Entry]))
        },
        test("get an entry") {
          for {
            transactionManager <- ZIO.service[TransactionManagerPg]
            entryOpt <- transactionManager.transaction {
              for {
                repo <- ZIO.service[EntryRepositoryPg]
                entry = Entry(
                  Entry.Headword("Test Entry", "en"),
                  Chunk(Entry.Definition("Test Definition", "en")),
                  1L.identifier[User.Student]
                )
                id <- repo.create(entry)
                entryOpt <- repo.get(id)
              } yield entryOpt
            }
          } yield assert(entryOpt.get)(
            zio.test.Assertion.equalTo(
              Entry(
                Entry.Headword("Test Entry", "en"),
                Chunk(Entry.Definition("Test Definition", "en")),
                1L.identifier[User.Student]
              )
            )
          )
        },
        test("delete an entry") {
          for {
            transactionManager <- ZIO.service[TransactionManagerPg]
            deleted <- transactionManager.transaction {
              for {
                repo <- ZIO.service[EntryRepositoryPg]
                entry = Entry(
                  Entry.Headword("Test Entry", "en"),
                  Chunk(Entry.Definition("Test Definition", "en")),
                  1L.identifier[User.Student]
                )
                id <- repo.create(entry)
                deleted <- repo.delete(id)
              } yield deleted
            }
          } yield assert(deleted)(zio.test.Assertion.isTrue)
        }
      )
    ).provideLayer(
      consoleColorTraceLogger >>> Fixture.layerWithEntryRepository
    ) @@ TestAspect.before(
      Fixture.testSystem
    ) @@ TestAspect.sequential // @@ TestAspect.ignore
}
