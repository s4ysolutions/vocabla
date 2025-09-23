package solutions.s4y.vocabla.infra.pgsql

import solutions.s4y.infra.pgsql.tx.TransactionManagerPg
import solutions.s4y.infra.pgsql.wrappers.pgWithTransaction
import solutions.s4y.vocabla.app.repo.UserRepository
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.{Tag, User}
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
            settings == UserRepository.emptyLearningSettings
          )
        },
        test("get learning settings for non-existing student returns empty settings") {
          for {
            repo <- ZIO.service[UserRepositoryPg]
            settings <- pgWithTransaction {
              repo.getLearningSettings(999L.identifier[User.Student])
            }
          } yield assertTrue(
            settings == UserRepository.emptyLearningSettings
          )
        },
        test("update and get learning settings") {
          for {
            transactionManager <- ZIO.service[TransactionManagerPg]
            retrievedSettings <- transactionManager.transaction {
              for {
                repo <- ZIO.service[UserRepositoryPg]
                newSettings = UserRepository.LearningSettings(
                  learnLanguages = Chunk("en", "es"),
                  knownLanguages = Chunk("fr", "de"),
                  tags = Chunk(1L.identifier[Tag], 2L.identifier[Tag])
                )
                _ <- repo.updateLearningSettings(1L.identifier[User.Student], newSettings)
                retrieved <- repo.getLearningSettings(1L.identifier[User.Student])
              } yield retrieved
            }
          } yield assertTrue(
            retrievedSettings.learnLanguages == Chunk("en", "es") &&
            retrievedSettings.knownLanguages == Chunk("fr", "de") &&
            retrievedSettings.tags == Chunk(1L.identifier[Tag], 2L.identifier[Tag])
          )
        },
        test("update learning settings for non-existing student should not fail") {
          for {
            repo <- ZIO.service[UserRepositoryPg]
            result <- pgWithTransaction {
              val newSettings = UserRepository.LearningSettings(
                learnLanguages = Chunk("en"),
                knownLanguages = Chunk("fr"),
                tags = Chunk(1L.identifier[Tag])
              )
              repo.updateLearningSettings(999L.identifier[User.Student], newSettings)
            }.either
          } yield assertTrue(result.isRight) // Should not fail, just update 0 rows
        },
        test("update learning settings with empty values") {
          for {
            transactionManager <- ZIO.service[TransactionManagerPg]
            retrievedSettings <- transactionManager.transaction {
              for {
                repo <- ZIO.service[UserRepositoryPg]
                // First set some values
                initialSettings = UserRepository.LearningSettings(
                  learnLanguages = Chunk("en", "es"),
                  knownLanguages = Chunk("fr"),
                  tags = Chunk(1L.identifier[Tag])
                )
                _ <- repo.updateLearningSettings(1L.identifier[User.Student], initialSettings)
                
                // Then update to empty settings
                emptySettings = UserRepository.emptyLearningSettings
                _ <- repo.updateLearningSettings(1L.identifier[User.Student], emptySettings)
                retrieved <- repo.getLearningSettings(1L.identifier[User.Student])
              } yield retrieved
            }
          } yield assertTrue(
            retrievedSettings == UserRepository.emptyLearningSettings
          )
        },
        test("update learning settings with complex data") {
          for {
            transactionManager <- ZIO.service[TransactionManagerPg]
            retrievedSettings <- transactionManager.transaction {
              for {
                repo <- ZIO.service[UserRepositoryPg]
                complexSettings = UserRepository.LearningSettings(
                  learnLanguages = Chunk("en", "es", "fr", "de", "it", "pt"),
                  knownLanguages = Chunk("ru", "zh", "ja", "ko"),
                  tags = Chunk(
                    1L.identifier[Tag], 
                    2L.identifier[Tag], 
                    3L.identifier[Tag],
                    100L.identifier[Tag],
                    999L.identifier[Tag]
                  )
                )
                _ <- repo.updateLearningSettings(1L.identifier[User.Student], complexSettings)
                retrieved <- repo.getLearningSettings(1L.identifier[User.Student])
              } yield retrieved
            }
          } yield assertTrue(
            retrievedSettings.learnLanguages.size == 6 &&
            retrievedSettings.knownLanguages.size == 4 &&
            retrievedSettings.tags.size == 5 &&
            retrievedSettings.learnLanguages.contains("en") &&
            retrievedSettings.learnLanguages.contains("pt") &&
            retrievedSettings.knownLanguages.contains("ru") &&
            retrievedSettings.knownLanguages.contains("ko") &&
            retrievedSettings.tags.contains(1L.identifier[Tag]) &&
            retrievedSettings.tags.contains(999L.identifier[Tag])
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
                specialSettings = UserRepository.LearningSettings(
                  learnLanguages = Chunk("en-US", "zh-CN", "es-MX"),
                  knownLanguages = Chunk("pt-BR", "fr-CA"),
                  tags = Chunk(1L.identifier[Tag])
                )
                _ <- repo.updateLearningSettings(1L.identifier[User.Student], specialSettings)
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
