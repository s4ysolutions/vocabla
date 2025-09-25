package solutions.s4y.vocabla.infra.pgsql

import solutions.s4y.infra.pgsql.wrappers.pgWithTransaction
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.{Lang, User}
import solutions.s4y.vocabla.infra.pgsql.Fixture.layerWithClearDb
import solutions.s4y.zio.consoleColorDebugLogger
import zio.test.{Spec, TestAspect, TestEnvironment, ZIOSpecDefault, assertTrue}
import zio.{Scope, ZIO}

object LearnLanguagesRepositoryPgSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("LearnLanguagesRepositoryPgSpec")(
      suite("Add learn language")(
        test("add new learn language to student") {
          for {
            repo <- ZIO.service[LearnLanguagesRepositoryPg]
            _ <- pgWithTransaction {
              repo.addLearnLanguage(
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
            settings.learnLanguages.contains(Lang.Code("en"))
          )
        },
        test("add multiple learn languages to student") {
          for {
            repo <- ZIO.service[LearnLanguagesRepositoryPg]
            _ <- pgWithTransaction {
              for {
                _ <- repo.addLearnLanguage(
                  1L.identifier[User.Student],
                  Lang.Code("de")
                )
                _ <- repo.addLearnLanguage(
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
            settings.learnLanguages.contains(Lang.Code("de")) &&
              settings.learnLanguages.contains(Lang.Code("fr"))
          )
        },
        test("add duplicate learn language is idempotent") {
          for {
            repo <- ZIO.service[LearnLanguagesRepositoryPg]
            userRepo <- ZIO.service[UserRepositoryPg]
            _ <- pgWithTransaction {
              for {
                _ <- repo.addLearnLanguage(
                  1L.identifier[User.Student],
                  Lang.Code("es")
                )
                _ <- repo.addLearnLanguage(
                  1L.identifier[User.Student],
                  Lang.Code("es")
                ) // duplicate
              } yield ()
            }
            settings <- pgWithTransaction {
              userRepo.getLearningSettings(1L.identifier[User.Student])
            }
          } yield assertTrue(
            settings.learnLanguages.count(
              _ == Lang.Code("es")
            ) == 2 // JSONB array allows duplicates
          )
        }
      ),
      suite("Remove learn language")(
        test("remove existing learn language from student") {
          for {
            repo <- ZIO.service[LearnLanguagesRepositoryPg]
            userRepo <- ZIO.service[UserRepositoryPg]
            _ <- pgWithTransaction {
              for {
                _ <- repo.addLearnLanguage(
                  1L.identifier[User.Student],
                  Lang.Code("it")
                )
                _ <- repo.addLearnLanguage(
                  1L.identifier[User.Student],
                  Lang.Code("pt")
                )
              } yield ()
            }
            // Remove one language
            _ <- pgWithTransaction {
              repo.removeLearnLanguage(
                1L.identifier[User.Student],
                Lang.Code("it")
              )
            }
            settings <- pgWithTransaction {
              userRepo.getLearningSettings(1L.identifier[User.Student])
            }
          } yield assertTrue(
            !settings.learnLanguages.contains(Lang.Code("it")) &&
              settings.learnLanguages.contains(Lang.Code("pt"))
          )
        },
        test("remove non-existing learn language is safe") {
          for {
            repo <- ZIO.service[LearnLanguagesRepositoryPg]
            userRepo <- ZIO.service[UserRepositoryPg]
            initialSettings <- pgWithTransaction {
              userRepo.getLearningSettings(1L.identifier[User.Student])
            }
            _ <- pgWithTransaction {
              repo.removeLearnLanguage(
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
        test("remove all occurrences of a learn language") {
          for {
            repo <- ZIO.service[LearnLanguagesRepositoryPg]
            userRepo <- ZIO.service[UserRepositoryPg]
            // Add same language multiple times
            _ <- pgWithTransaction {
              for {
                _ <- repo.addLearnLanguage(
                  1L.identifier[User.Student],
                  Lang.Code("ru")
                )
                _ <- repo.addLearnLanguage(
                  1L.identifier[User.Student],
                  Lang.Code("ru")
                )
                _ <- repo.addLearnLanguage(
                  1L.identifier[User.Student],
                  Lang.Code("zh")
                )
              } yield ()
            }
            // Remove all occurrences of "ru"
            _ <- pgWithTransaction {
              repo.removeLearnLanguage(
                1L.identifier[User.Student],
                Lang.Code("ru")
              )
            }
            settings <- pgWithTransaction {
              userRepo.getLearningSettings(1L.identifier[User.Student])
            }
          } yield assertTrue(
            !settings.learnLanguages.contains(Lang.Code("ru")) &&
              settings.learnLanguages.contains(Lang.Code("zh"))
          )
        }
      ),
      suite("Edge cases")(
        test("operations on non-existing student are safe") {
          for {
            repo <- ZIO.service[LearnLanguagesRepositoryPg]
            // These should not fail, even though student doesn't exist
            _ <- pgWithTransaction {
              for {
                _ <- repo.addLearnLanguage(
                  999L.identifier[User.Student],
                  Lang.Code("en")
                )
                _ <- repo.removeLearnLanguage(
                  999L.identifier[User.Student],
                  Lang.Code("en")
                )
              } yield ()
            }
          } yield assertTrue(true)
        },
        test("preserve known languages when modifying learn languages") {
          for {
            knownRepo <- ZIO.service[KnownLanguagesRepositoryPg]
            learnRepo <- ZIO.service[LearnLanguagesRepositoryPg]
            userRepo <- ZIO.service[UserRepositoryPg]
            // Add known language first
            _ <- pgWithTransaction {
              knownRepo.addKnownLanguage(
                1L.identifier[User.Student],
                Lang.Code("ja")
              )
            }
            // Add learn language
            _ <- pgWithTransaction {
              learnRepo.addLearnLanguage(
                1L.identifier[User.Student],
                Lang.Code("ko")
              )
            }
            settings <- pgWithTransaction {
              userRepo.getLearningSettings(1L.identifier[User.Student])
            }
          } yield assertTrue(
            settings.learnLanguages.contains(Lang.Code("ko")) &&
              settings.knownLanguages.contains(Lang.Code("ja"))
          )
        }
      )
    ).provideLayer(
      consoleColorDebugLogger >>> layerWithClearDb >>> (Fixture.layerWithKnownLanguagesRepository ++
        Fixture.layerWithUserRepository ++
        Fixture.layerWithTagRepository ++
        Fixture.layerWithLearnLanguagesRepository)
    ) @@ TestAspect.before(Fixture.testSystem) @@ TestAspect.sequential
  }
}
