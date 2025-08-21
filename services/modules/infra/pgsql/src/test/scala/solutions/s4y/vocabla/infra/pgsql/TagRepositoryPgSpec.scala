package solutions.s4y.vocabla.infra.pgsql

import solutions.s4y.vocabla.app.repo.TagRepository
import solutions.s4y.vocabla.app.repo.tx.TransactionManager
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.{Student, Tag}
import solutions.s4y.zio.consoleColorDebugLogger
import zio.ZIO
import zio.test.{Spec, TestAspect, ZIOSpecDefault, assertTrue}

object TagRepositoryPgSpec extends ZIOSpecDefault {

  override def spec = suite("TagRepositoryPgSpec")(
    suite("Create") {
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
      }
    }
  ).provideLayer(
    consoleColorDebugLogger >>> Fixture.layer
  ) @@ TestAspect.before(Fixture.testSystem)
}
