package solutions.s4y.vocabla.infra.pgsql

import solutions.s4y.infra.pgsql.wrappers.pgWithTransaction
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.{Lang, User}
import solutions.s4y.vocabla.infra.pgsql.Fixture.layerWithClearDb
import solutions.s4y.zio.consoleColorDebugLogger
import zio.test.{Spec, TestAspect, TestEnvironment, ZIOSpecDefault, assertTrue}
import zio.{Scope, ZIO}

object KnownLanguagesRepositoryPgSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("KnownLanguagesRepositoryPgSpec")(
      suite("Add known language")(
        test("add new known language to student") {
          for {
            repo <- ZIO.service[KnownLanguagesRepositoryPg]
            _ <- pgWithTransaction {
              repo.addKnownLanguage(
                1L.identifier[User.Student],
                Lang.Code("en")
              )
            }
            // Verify by checking the database directly
            userRepo <- ZIO.service[UserRepositoryPg]
            settings <- pgWithTransaction {
              userRepo.getLearningSettings(1L.identifier[User.Student])
            }
          } yield assertTrue(
            settings.knownLanguages.contains(Lang.Code("en"))
          )
        },
        test("add multiple known languages to student") {
          for {
            repo <- ZIO.service[KnownLanguagesRepositoryPg]
            _ <- pgWithTransaction {
              for {
                _ <- repo.addKnownLanguage(
                  1L.identifier[User.Student],
                  Lang.Code("de")
                )
                _ <- repo.addKnownLanguage(
                  1L.identifier[User.Student],
                  Lang.Code("fr")
                )
              } yield ()
            }
            // Verify both languages were added
            userRepo <- ZIO.service[UserRepositoryPg]
            settings <- pgWithTransaction {
              userRepo.getLearningSettings(1L.identifier[User.Student])
            }
          } yield assertTrue(
            settings.knownLanguages.contains(Lang.Code("de")) &&
              settings.knownLanguages.contains(Lang.Code("fr"))
          )
        },
        test("add duplicate known language creates duplicate entries") {
          for {
            repo <- ZIO.service[KnownLanguagesRepositoryPg]
            userRepo <- ZIO.service[UserRepositoryPg]
            _ <- pgWithTransaction {
              for {
                _ <- repo.addKnownLanguage(
                  1L.identifier[User.Student],
                  Lang.Code("es")
                )
                _ <- repo.addKnownLanguage(
                  1L.identifier[User.Student],
                  Lang.Code("es")
                ) // duplicate
              } yield ()
            }
            settings <- pgWithTransaction {
              userRepo.getLearningSettings(1L.identifier[User.Student])
            }
          } yield assertTrue(
            settings.knownLanguages.count(
              _ == Lang.Code("es")
            ) == 2 // JSONB array allows duplicates
          )
        }
      ),
      suite("Remove known language")(
        test("remove existing known language from student") {
          for {
            repo <- ZIO.service[KnownLanguagesRepositoryPg]
            userRepo <- ZIO.service[UserRepositoryPg]
            _ <- pgWithTransaction {
              for {
                _ <- repo.addKnownLanguage(
                  1L.identifier[User.Student],
                  Lang.Code("it")
                )
                _ <- repo.addKnownLanguage(
                  1L.identifier[User.Student],
                  Lang.Code("pt")
                )
              } yield ()
            }
            // Remove one language
            _ <- pgWithTransaction {
              repo.removeKnownLanguage(
                1L.identifier[User.Student],
                Lang.Code("it")
              )
            }
            settings <- pgWithTransaction {
              userRepo.getLearningSettings(1L.identifier[User.Student])
            }
          } yield assertTrue(
            !settings.knownLanguages.contains(Lang.Code("it")) &&
              settings.knownLanguages.contains(Lang.Code("pt"))
          )
        },
        test("remove non-existing known language is safe") {
          for {
            repo <- ZIO.service[KnownLanguagesRepositoryPg]
            userRepo <- ZIO.service[UserRepositoryPg]
            initialSettings <- pgWithTransaction {
              userRepo.getLearningSettings(1L.identifier[User.Student])
            }
            _ <- pgWithTransaction {
              repo.removeKnownLanguage(
                1L.identifier[User.Student],
                Lang.Code("nonexistent")
              )
            }
            finalSettings <- pgWithTransaction {
              userRepo.getLearningSettings(1L.identifier[User.Student])
            }
          } yield assertTrue(
            initialSettings == finalSettings
          )
        },
        test("remove all occurrences of a known language") {
          for {
            repo <- ZIO.service[KnownLanguagesRepositoryPg]
            userRepo <- ZIO.service[UserRepositoryPg]
            // Add same language multiple times
            _ <- pgWithTransaction {
              for {
                _ <- repo.addKnownLanguage(
                  1L.identifier[User.Student],
                  Lang.Code("ru")
                )
                _ <- repo.addKnownLanguage(
                  1L.identifier[User.Student],
                  Lang.Code("ru")
                )
                _ <- repo.addKnownLanguage(
                  1L.identifier[User.Student],
                  Lang.Code("zh")
                )
              } yield ()
            }
            // Remove all occurrences of "ru"
            _ <- pgWithTransaction {
              repo.removeKnownLanguage(
                1L.identifier[User.Student],
                Lang.Code("ru")
              )
            }
            settings <- pgWithTransaction {
              userRepo.getLearningSettings(1L.identifier[User.Student])
            }
          } yield assertTrue(
            !settings.knownLanguages.contains(Lang.Code("ru")) &&
              settings.knownLanguages.contains(Lang.Code("zh"))
          )
        }
      ),
      suite("Edge cases")(
        test("operations on non-existing student are safe") {
          for {
            repo <- ZIO.service[KnownLanguagesRepositoryPg]
            // These should not fail, even though student doesn't exist
            _ <- pgWithTransaction {
              for {
                _ <- repo.addKnownLanguage(
                  999L.identifier[User.Student],
                  Lang.Code("en")
                )
                _ <- repo.removeKnownLanguage(
                  999L.identifier[User.Student],
                  Lang.Code("en")
                )
              } yield ()
            }
          } yield assertTrue(true)
        },
        test("preserve learn languages when modifying known languages") {
          for {
            knownRepo <- ZIO.service[KnownLanguagesRepositoryPg]
            learnRepo <- ZIO.service[LearnLanguagesRepositoryPg]
            userRepo <- ZIO.service[UserRepositoryPg]
            // Add learn language first
            _ <- pgWithTransaction {
              learnRepo.addLearnLanguage(
                1L.identifier[User.Student],
                Lang.Code("ja")
              )
            }
            // Add known language
            _ <- pgWithTransaction {
              knownRepo.addKnownLanguage(
                1L.identifier[User.Student],
                Lang.Code("ko")
              )
            }
            settings <- pgWithTransaction {
              userRepo.getLearningSettings(1L.identifier[User.Student])
            }
          } yield assertTrue(
            settings.learnLanguages.contains(Lang.Code("ja")) &&
              settings.knownLanguages.contains(Lang.Code("ko"))
          )
        }
      ),
      suite("JSON structure integrity")(
        test(
          "adding known language preserves JSON structure when no prior settings exist"
        ) {
          for {
            repo <- ZIO.service[KnownLanguagesRepositoryPg]
            userRepo <- ZIO.service[UserRepositoryPg]
            // Clear any existing settings by getting a fresh student ID
            studentId = 1L.identifier[User.Student]
            _ <- pgWithTransaction {
              repo.addKnownLanguage(studentId, Lang.Code("nl"))
            }
            settings <- pgWithTransaction {
              userRepo.getLearningSettings(studentId)
            }
          } yield assertTrue(
            settings.knownLanguages.contains(Lang.Code("nl")) &&
              settings.learnLanguages.isEmpty // Should have empty array, not null
          )
        },
        test("removing from empty known languages list is safe") {
          for {
            repo <- ZIO.service[KnownLanguagesRepositoryPg]
            studentId = 3L.identifier[User.Student]
            // Try to remove from empty list
            _ <- pgWithTransaction {
              repo.removeKnownLanguage(studentId, Lang.Code("nonexistent"))
            }
          } yield assertTrue(true) // Should not throw exception
        }
      )
    ).provideLayer(
      consoleColorDebugLogger >>> layerWithClearDb >>> (
        Fixture.layerWithKnownLanguagesRepository ++
          Fixture.layerWithUserRepository ++
          Fixture.layerWithTagRepository ++
          Fixture.layerWithLearnLanguagesRepository)
    ) @@ TestAspect.before(Fixture.testSystem) @@ TestAspect.sequential
  }
}
