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
      suite("Entry: create/get/delete")(
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
      ),
      suite("Entries: get")(
        test("get entries without filters - returns all entries") {
          for {
            transactionManager <- ZIO.service[TransactionManagerPg]
            results <- transactionManager.transaction {
              for {
                repo <- ZIO.service[EntryRepositoryPg]
                // Create test entries
                entry1 = Entry(
                  Entry.Headword("Hello", "en"),
                  Chunk(Entry.Definition("Greeting", "en")),
                  1L.identifier[User.Student]
                )
                entry2 = Entry(
                  Entry.Headword("Hola", "es"),
                  Chunk(Entry.Definition("Saludo", "es")),
                  2L.identifier[User.Student]
                )
                id1 <- repo.create(entry1)
                id2 <- repo.create(entry2)
                entries <- repo.get()
              } yield entries
            }
          } yield assert(results.size)(zio.test.Assertion.equalTo(2))
        },
        test("get entries filtered by ownerId") {
          for {
            transactionManager <- ZIO.service[TransactionManagerPg]
            results <- transactionManager.transaction {
              for {
                repo <- ZIO.service[EntryRepositoryPg]
                // Create entries with different owners
                entry1 = Entry(
                  Entry.Headword("Word1", "en"),
                  Chunk(Entry.Definition("Definition1", "en")),
                  1L.identifier[User.Student]
                )
                entry2 = Entry(
                  Entry.Headword("Word2", "en"),
                  Chunk(Entry.Definition("Definition2", "en")),
                  2L.identifier[User.Student]
                )
                entry3 = Entry(
                  Entry.Headword("Word3", "en"),
                  Chunk(Entry.Definition("Definition3", "en")),
                  1L.identifier[User.Student]
                )
                id1 <- repo.create(entry1)
                id2 <- repo.create(entry2)
                id3 <- repo.create(entry3)
                // Filter by owner 1
                entries <- repo.get(ownerId = Some(1L.identifier[User]))
              } yield entries
            }
          } yield assert(results.size)(zio.test.Assertion.equalTo(2)) &&
            assert(results.forall(_.e.ownerId == 1L.identifier[User.Student]))(zio.test.Assertion.isTrue)
        },
        test("get entries filtered by language codes") {
          for {
            transactionManager <- ZIO.service[TransactionManagerPg]
            results <- transactionManager.transaction {
              for {
                repo <- ZIO.service[EntryRepositoryPg]
                // Create entries in different languages
                entryEn = Entry(
                  Entry.Headword("Hello", "en"),
                  Chunk(Entry.Definition("Greeting", "en")),
                  1L.identifier[User.Student]
                )
                entryEs = Entry(
                  Entry.Headword("Hola", "es"),
                  Chunk(Entry.Definition("Saludo", "es")),
                  1L.identifier[User.Student]
                )
                entryFr = Entry(
                  Entry.Headword("Bonjour", "fr"),
                  Chunk(Entry.Definition("Salutation", "fr")),
                  1L.identifier[User.Student]
                )
                id1 <- repo.create(entryEn)
                id2 <- repo.create(entryEs)
                id3 <- repo.create(entryFr)
                // Filter by English and Spanish
                entries <- repo.get(langCodes = Chunk("en", "es"))
              } yield entries
            }
          } yield assert(results.size)(zio.test.Assertion.equalTo(2)) &&
            assert(results.forall(e => e.e.headword.langCode == "en" || e.e.headword.langCode == "es"))(zio.test.Assertion.isTrue)
        },
        test("get entries filtered by text search in word") {
          for {
            transactionManager <- ZIO.service[TransactionManagerPg]
            results <- transactionManager.transaction {
              for {
                repo <- ZIO.service[EntryRepositoryPg]
                // Create entries with different words
                entry1 = Entry(
                  Entry.Headword("Hello World", "en"),
                  Chunk(Entry.Definition("Greeting", "en")),
                  1L.identifier[User.Student]
                )
                entry2 = Entry(
                  Entry.Headword("Goodbye", "en"),
                  Chunk(Entry.Definition("Farewell", "en")),
                  1L.identifier[User.Student]
                )
                entry3 = Entry(
                  Entry.Headword("Hello There", "en"),
                  Chunk(Entry.Definition("Another greeting", "en")),
                  1L.identifier[User.Student]
                )
                id1 <- repo.create(entry1)
                id2 <- repo.create(entry2)
                id3 <- repo.create(entry3)
                // Search for "Hello"
                entries <- repo.get(text = Some("Hello"))
              } yield entries
            }
          } yield assert(results.size)(zio.test.Assertion.equalTo(2)) &&
            assert(results.forall(_.e.headword.word.contains("Hello")))(zio.test.Assertion.isTrue)
        },
        test("get entries filtered by text search in definitions") {
          for {
            transactionManager <- ZIO.service[TransactionManagerPg]
            results <- transactionManager.transaction {
              for {
                repo <- ZIO.service[EntryRepositoryPg]
                // Create entries with different definitions
                entry1 = Entry(
                  Entry.Headword("Word1", "en"),
                  Chunk(Entry.Definition("A special greeting", "en")),
                  1L.identifier[User.Student]
                )
                entry2 = Entry(
                  Entry.Headword("Word2", "en"),
                  Chunk(Entry.Definition("A farewell", "en")),
                  1L.identifier[User.Student]
                )
                entry3 = Entry(
                  Entry.Headword("Word3", "en"),
                  Chunk(Entry.Definition("Another special word", "en")),
                  1L.identifier[User.Student]
                )
                id1 <- repo.create(entry1)
                id2 <- repo.create(entry2)
                id3 <- repo.create(entry3)
                // Search for "special"
                entries <- repo.get(text = Some("special"))
              } yield entries
            }
          } yield assert(results.size)(zio.test.Assertion.equalTo(2)) &&
            assert(results.forall(e =>
              e.e.definitions.exists(_.definition.toLowerCase.contains("special"))
            ))(zio.test.Assertion.isTrue)
        },
        test("get entries with combined filters") {
          for {
            transactionManager <- ZIO.service[TransactionManagerPg]
            results <- transactionManager.transaction {
              for {
                repo <- ZIO.service[EntryRepositoryPg]
                // Create diverse entries
                entry1 = Entry(
                  Entry.Headword("Hello", "en"),
                  Chunk(Entry.Definition("Greeting word", "en")),
                  1L.identifier[User.Student]
                )
                entry2 = Entry(
                  Entry.Headword("Hello", "es"),
                  Chunk(Entry.Definition("Palabra de saludo", "es")),
                  1L.identifier[User.Student]
                )
                entry3 = Entry(
                  Entry.Headword("Goodbye", "en"),
                  Chunk(Entry.Definition("Farewell word", "en")),
                  1L.identifier[User.Student]
                )
                entry4 = Entry(
                  Entry.Headword("Hello", "en"),
                  Chunk(Entry.Definition("Another greeting", "en")),
                  2L.identifier[User.Student]
                )
                id1 <- repo.create(entry1)
                id2 <- repo.create(entry2)
                id3 <- repo.create(entry3)
                id4 <- repo.create(entry4)
                // Filter by owner=1, lang=en, text="Hello"
                entries <- repo.get(
                  ownerId = Some(1L.identifier[User]),
                  langCodes = Chunk("en"),
                  text = Some("Hello")
                )
              } yield entries
            }
          } yield assert(results.size)(zio.test.Assertion.equalTo(1)) &&
            assert(results.head.e)(zio.test.Assertion.equalTo(
              Entry(
                Entry.Headword("Hello", "en"),
                Chunk(Entry.Definition("Greeting word", "en")),
                1L.identifier[User.Student]
              )
            ))
        },
        test("get entries with limit") {
          for {
            transactionManager <- ZIO.service[TransactionManagerPg]
            results <- transactionManager.transaction {
              for {
                repo <- ZIO.service[EntryRepositoryPg]
                // Create multiple entries
                testEntries = (1 to 5).map { i =>
                  Entry(
                    Entry.Headword(s"Word$i", "en"),
                    Chunk(Entry.Definition(s"Definition$i", "en")),
                    1L.identifier[User.Student]
                  )
                }
                _ <- ZIO.foreachDiscard(testEntries)(repo.create)
                // Get with limit of 3
                entries <- repo.get(limit = 3)
              } yield entries
            }
          } yield assert(results.size)(zio.test.Assertion.equalTo(3))
        },
        test("get entries returns empty map when no matches") {
          for {
            transactionManager <- ZIO.service[TransactionManagerPg]
            results <- transactionManager.transaction {
              for {
                repo <- ZIO.service[EntryRepositoryPg]
                // Create an entry
                entry = Entry(
                  Entry.Headword("Hello", "en"),
                  Chunk(Entry.Definition("Greeting", "en")),
                  1L.identifier[User.Student]
                )
                id <- repo.create(entry)
                // Search for non-existent text
                entries <- repo.get(text = Some("NonExistentWord"))
              } yield entries
            }
          } yield assert(results)(zio.test.Assertion.isEmpty)
        },
        test("get entries case insensitive text search") {
          for {
            transactionManager <- ZIO.service[TransactionManagerPg]
            result <- transactionManager.transaction {
              for {
                repo <- ZIO.service[EntryRepositoryPg]
                entry = Entry(
                  Entry.Headword("Hello World", "en"),
                  Chunk(Entry.Definition("GREETING", "en")),
                  1L.identifier[User.Student]
                )
                id <- repo.create(entry)
                // Search with different case
                entriesLower <- repo.get(text = Some("hello"))
                entriesUpper <- repo.get(text = Some("GREETING"))
              } yield (entriesLower, entriesUpper)
            }
          } yield assert(result._1.size)(zio.test.Assertion.equalTo(1)) &&
            assert(result._2.size)(zio.test.Assertion.equalTo(1))
        }
      )
    ).provideLayer(
      consoleColorTraceLogger >>> Fixture.layerWithEntryRepository
    ) @@ TestAspect.before(
      Fixture.testSystem
    ) @@ TestAspect.sequential // @@ TestAspect.ignore
}
