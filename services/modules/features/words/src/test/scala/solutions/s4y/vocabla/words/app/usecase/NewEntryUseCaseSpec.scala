package solutions.s4y.vocabla.words.app.usecase

import solutions.s4y.vocabla.id.IdFactory
import solutions.s4y.vocabla.infrastructure.mvstore.KeyValueMVStore.makeMVStoreMemory
import solutions.s4y.vocabla.words.domain.model.Entity
import solutions.s4y.vocabla.words.infra.kv.mvstore.MVStoreLive
import zio.test.*
import zio.{Scope, UIO, ZIO, ZLayer}

object NewEntryUseCaseSpec extends ZIOSpecDefault {
  // Simple ID factory for testing that generates sequential IDs
  class TestIdFactory extends IdFactory[Entity.Id] {
    private var currentId = 10
    override def next: UIO[Entity.Id] = ZIO.succeed {
      currentId += 1
      currentId.asInstanceOf[Entity.Id]
    }
  }

  def spec: Spec[TestEnvironment & Scope, Any] = suite("NewEntryUseCase")(
    test("should create a new entry") {
      for {
        _ <- newEntryUseCase(
          "test",
          "en",
          "test definition",
          "es",
          1L,
          List("tag1", "tag2")
        )
      } yield assertTrue(true)
    }
  ).provide(
    (ZLayer.scoped(makeMVStoreMemory()) ++ ZLayer.succeed(
      new TestIdFactory
    )) >>> MVStoreLive.layers
  )
}
