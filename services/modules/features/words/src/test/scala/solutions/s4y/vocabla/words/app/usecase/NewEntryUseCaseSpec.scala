package solutions.s4y.vocabla.words.app.usecase

import solutions.s4y.vocabla.lang.infra.langRoRepository
import solutions.s4y.vocabla.words.infra.kv.mvstore.Fixture
import solutions.s4y.vocabla.words.infra.kv.mvstore.Fixture.{
  ID,
  layerIdFactory,
  layerMVStore,
  given
}
import zio.test.*
import zio.{ZIO, ZLayer}

object NewEntryUseCaseSpec extends ZIOSpecDefault {
  def spec = suite("NewEntryUseCase")(
    suite("use service")(
      test("should create a service instance") {
        for {
          service <- ZIO.service[WordsService]
        } yield assertTrue(true)
      }
    ).provide(
      (layerMVStore ++ layerIdFactory) >>> WordsServiceMVStore
        .makeLayer[Fixture.ID]
    )
  )
}
