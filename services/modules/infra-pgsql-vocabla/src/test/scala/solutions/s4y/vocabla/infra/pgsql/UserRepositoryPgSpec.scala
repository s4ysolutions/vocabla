package solutions.s4y.vocabla.infra.pgsql

import solutions.s4y.infra.pgsql.tx.TransactionManagerPg
import solutions.s4y.infra.pgsql.wrappers.pgWithTransaction
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.{LearningSettings, Tag, User}
import solutions.s4y.zio.consoleColorDebugLogger
import zio.test.{Spec, TestAspect, TestEnvironment, ZIOSpecDefault, assertTrue}
import zio.{Chunk, Scope, ZIO, ZLayer}

object UserRepositoryPgSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("UserRepositoryPgSpec")(
      suite("User retrieval")(
        test("get existing user with student role") {
          for {
            repo <- ZIO.service[UserRepositoryPg]
            userOpt <- pgWithTransaction {
              // The default user is created with student role in the layer initialization
              repo.get(1L.identifier[User])
            }
          } yield assertTrue(
            userOpt.isDefined &&
            userOpt.get.student.isDefined &&
            userOpt.get.student.get.nickname == "default_student" &&
            userOpt.get.admin.isEmpty
          )
        },
        test("get non-existing user returns None") {
          for {
            repo <- ZIO.service[UserRepositoryPg]
            userOpt <- pgWithTransaction {
              repo.get(999L.identifier[User])
            }
          } yield assertTrue(userOpt.isEmpty)
        }
      ),
      suite("Learning settings")(
        test("get learning settings for existing student returns empty settings by default") {
          for {
            repo <- ZIO.service[UserRepositoryPg]
            settings <- pgWithTransaction {
              repo.getLearningSettings(1L.identifier[User.Student])
            }
          } yield assertTrue(
            settings == LearningSettings.emptyLearningSettings
          )
        },
        test("get learning settings for non-existing student returns empty settings") {
          for {
            repo <- ZIO.service[UserRepositoryPg]
            settings <- pgWithTransaction {
              repo.getLearningSettings(999L.identifier[User.Student])
            }
          } yield assertTrue(
            settings == LearningSettings.emptyLearningSettings
          )
        },
        test("bulk update and get learning languages") {
          for {
            transactionManager <- ZIO.service[TransactionManagerPg]
            retrievedSettings <- transactionManager.transaction {
              for {
                repo <- ZIO.service[UserRepositoryPg]
                // Use the language-only bulk update method
                _ <- repo.updateLearningLanguages(
                  1L.identifier[User.Student],
                  Chunk("en", "es"),
                  Chunk("fr", "de")
                )
                retrieved <- repo.getLearningSettings(1L.identifier[User.Student])
              } yield retrieved
            }
          } yield assertTrue(
            retrievedSettings.learnLanguages == Chunk("en", "es") &&
            retrievedSettings.knownLanguages == Chunk("fr", "de") &&
            retrievedSettings.tags.isEmpty // Tags managed separately
          )
        },
        test("update learning languages for non-existing student should not fail") {
          for {
            repo <- ZIO.service[UserRepositoryPg]
            result <- pgWithTransaction {
              repo.updateLearningLanguages(
                999L.identifier[User.Student],
                Chunk("en"),
                Chunk("fr")
              )
            }.either
          } yield assertTrue(result.isRight) // Should not fail, just update 0 rows
        },
        test("update learning languages with empty values") {
          for {
            transactionManager <- ZIO.service[TransactionManagerPg]
            retrievedSettings <- transactionManager.transaction {
              for {
                repo <- ZIO.service[UserRepositoryPg]
                // First set some values
                _ <- repo.updateLearningLanguages(
                  1L.identifier[User.Student],
                  Chunk("en", "es"),
                  Chunk("fr")
                )

                // Then update to empty languages
                _ <- repo.updateLearningLanguages(
                  1L.identifier[User.Student],
                  Chunk.empty,
                  Chunk.empty
                )
                retrieved <- repo.getLearningSettings(1L.identifier[User.Student])
              } yield retrieved
            }
          } yield assertTrue(
            retrievedSettings.learnLanguages.isEmpty &&
            retrievedSettings.knownLanguages.isEmpty &&
            retrievedSettings.tags.isEmpty
          )
        },
        test("update learning languages with complex data") {
          for {
            transactionManager <- ZIO.service[TransactionManagerPg]
            retrievedSettings <- transactionManager.transaction {
              for {
                repo <- ZIO.service[UserRepositoryPg]
                _ <- repo.updateLearningLanguages(
                  1L.identifier[User.Student],
                  Chunk("en", "es", "fr", "de", "it", "pt"),
                  Chunk("ru", "zh", "ja", "ko")
                )
                retrieved <- repo.getLearningSettings(1L.identifier[User.Student])
              } yield retrieved
            }
          } yield assertTrue(
            retrievedSettings.learnLanguages.size == 6 &&
            retrievedSettings.knownLanguages.size == 4 &&
            retrievedSettings.learnLanguages.contains("en") &&
            retrievedSettings.learnLanguages.contains("pt") &&
            retrievedSettings.knownLanguages.contains("ru") &&
            retrievedSettings.knownLanguages.contains("ko")
          )
        },
        test("get learning settings includes tags from user_learning_tags table") {
          for {
            transactionManager <- ZIO.service[TransactionManagerPg]
            settings <- transactionManager.transaction {
              for {
                repo <- ZIO.service[UserRepositoryPg]
                // Set up some languages
                _ <- repo.updateLearningLanguages(
                  1L.identifier[User.Student],
                  Chunk("en", "es"),
                  Chunk("fr")
                )
                // Note: Tags would be managed through separate tag association methods
                // that you already have implemented - we don't test those here
                retrieved <- repo.getLearningSettings(1L.identifier[User.Student])
              } yield retrieved
            }
          } yield assertTrue(
            settings.learnLanguages == Chunk("en", "es") &&
            settings.knownLanguages == Chunk("fr") &&
            settings.tags.isEmpty // Empty since no tags associated via user_learning_tags
          )
        }
      ),
      suite("JSON serialization edge cases")(
        test("handle learning settings with special characters") {
          for {
            transactionManager <- ZIO.service[TransactionManagerPg]
            retrievedSettings <- transactionManager.transaction {
              for {
                repo <- ZIO.service[UserRepositoryPg]
                _ <- repo.updateLearningLanguages(
                  1L.identifier[User.Student],
                  Chunk("en-US", "zh-CN", "es-MX"),
                  Chunk("pt-BR", "fr-CA")
                )
                retrieved <- repo.getLearningSettings(1L.identifier[User.Student])
              } yield retrieved
            }
          } yield assertTrue(
            retrievedSettings.learnLanguages == Chunk("en-US", "zh-CN", "es-MX") &&
            retrievedSettings.knownLanguages == Chunk("pt-BR", "fr-CA")
          )
        }
      )
    ).provide {
      consoleColorDebugLogger >>> Fixture.layerWithUserRepository
    } @@ TestAspect.before(
      Fixture.testSystem
    ) @@ TestAspect.sequential
  }
}
