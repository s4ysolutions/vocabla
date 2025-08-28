package solutions.s4y.vocabla.infra.pgsql

import solutions.s4y.infra.pgsql.tx.TransactionManagerPg
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.{Tag, User}
import solutions.s4y.zio.consoleColorDebugLogger
import zio.test.{Spec, TestAspect, TestEnvironment, ZIOSpecDefault, assertTrue}
import zio.{Scope, ZIO, ZLayer}

object TagRepositoryPgSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("TagRepositoryPgSpec")(
      suite("Create")(
        test("create a tag") {
          for {
            transactionManager <- ZIO.service[TransactionManagerPg]
            repo <- ZIO.service[TagRepositoryPg]
            id <- transactionManager.transaction(
              repo.create(Tag("Test Tag", 1L.identifier[User.Student]))
            )
          } yield assertTrue(id == 1.identifier[Tag])
        },
        test("get a tag") {
          for {
            transactionManager <- ZIO.service[TransactionManagerPg]
            tagOpt <- transactionManager.transaction {
              for {
                repo <- ZIO.service[TagRepositoryPg]
                id <- repo.create(Tag("Test Tag", 1L.identifier[User.Student]))
                tagOpt <- repo.get(id)
              } yield tagOpt
            }
          } yield assertTrue(
            tagOpt.get == Tag("Test Tag", 1L.identifier[User.Student])
          )
        },
        test("update a tag") {
          for {
            transactionManager <- ZIO.service[TransactionManagerPg]
            updated <- transactionManager.transaction {
              for {
                repo <- ZIO.service[TagRepositoryPg]
                id <- repo.create(Tag("Test Tag", 1L.identifier[User.Student]))
                _ <- repo.updateLabel(id, "Updated Tag")
                tagOpt <- repo.get(id)
              } yield tagOpt
            }
          } yield assertTrue(
            updated.get == Tag("Updated Tag", 1L.identifier[User.Student])
          )
        }
      )
    ).provide {
      consoleColorDebugLogger >>> Fixture.layer
    } @@ TestAspect.before(Fixture.testSystem) @@ TestAspect.sequential
  }
}
