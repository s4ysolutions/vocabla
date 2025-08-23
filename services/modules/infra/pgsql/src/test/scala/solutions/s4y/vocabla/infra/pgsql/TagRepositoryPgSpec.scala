package solutions.s4y.vocabla.infra.pgsql

import solutions.s4y.vocabla.app.repo.TagRepository
import solutions.s4y.vocabla.app.repo.tx.TransactionManager
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.{Student, Tag}
import solutions.s4y.zio.consoleColorDebugLogger
import zio.{Scope, ZIO}
import zio.test.{Spec, TestAspect, TestEnvironment, ZIOSpecDefault, assertTrue}

object TagRepositoryPgSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("TagRepositoryPgSpec")(
    suite("Create")(
      test("create a tag") {
        for {
          transactionManager <- ZIO.service[TransactionManager]
          id <- transactionManager.transaction {
            for {
              repo <- ZIO.service[TagRepository]
              id <- repo.create(Tag("Test Tag", 1L.identifier[Student]))
            } yield id
          }
        } yield assertTrue(id == 1.identifier[Tag])
      },
      test("get a tag") {
        for {
          transactionManager <- ZIO.service[TransactionManager]
          tagOpt <- transactionManager.transaction {
            for {
              repo <- ZIO.service[TagRepository]
              id <- repo.create(Tag("Test Tag", 1L.identifier[Student]))
              tagOpt <- repo.get(id)
            } yield tagOpt
          }
        } yield assertTrue(
          tagOpt.get == Tag("Test Tag", 1L.identifier[Student])
        )
      },
      test("update a tag") {
        for {
          transactionManager <- ZIO.service[TransactionManager]
          updated <- transactionManager.transaction {
            for {
              repo <- ZIO.service[TagRepository]
              id <- repo.create(Tag("Test Tag", 1L.identifier[Student]))
              _ <- repo.updateLabel(id, "Updated Tag")
              tagOpt <- repo.get(id)
            } yield tagOpt
          }
        } yield assertTrue(
          updated.get == Tag("Updated Tag", 1L.identifier[Student])
        )
      }
    )
  ).provideLayer(
    consoleColorDebugLogger >>> Fixture.layer
  ) @@ TestAspect.before(Fixture.testSystem) @@ TestAspect.sequential
}
