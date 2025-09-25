package solutions.s4y.vocabla.infra.pgsql

import solutions.s4y.infra.pgsql.wrappers.pgWithTransaction
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.{LearningSettings, User}
import solutions.s4y.vocabla.infra.pgsql.Fixture.layerWithClearDb
import solutions.s4y.zio.consoleColorDebugLogger
import zio.test.{Spec, TestAspect, TestEnvironment, ZIOSpecDefault, assertTrue}
import zio.{Scope, ZIO, ZLayer}

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
        test(
          "get learning settings for existing student returns empty settings by default"
        ) {
          for {
            repo <- ZIO.service[UserRepositoryPg]
            settings <- pgWithTransaction {
              repo.getLearningSettings(1L.identifier[User.Student])
            }
          } yield assertTrue(
            settings == LearningSettings.emptyLearningSettings
          )
        },
        test(
          "get learning settings for non-existing student returns empty settings"
        ) {
          for {
            repo <- ZIO.service[UserRepositoryPg]
            settings <- pgWithTransaction {
              repo.getLearningSettings(999L.identifier[User.Student])
            }
          } yield assertTrue(
            settings == LearningSettings.emptyLearningSettings
          )
        }
      )
    ).provide {
      consoleColorDebugLogger >>> layerWithClearDb >>> (Fixture.layerWithUserRepository ++ Fixture.layerWithTagRepository)
    } @@ TestAspect.before(
      Fixture.testSystem
    ) @@ TestAspect.sequential
  }
}
