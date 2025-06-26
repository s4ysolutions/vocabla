package solutions.s4y.vocabla.infrastructure.mvstore

import org.h2.mvstore.MVStore
import zio.{ZIO, ZLayer}
import zio.test.{Spec, ZIOSpecDefault, assertTrue}

object KeyValueMVStoreSpec extends ZIOSpecDefault {
  case class Entity(content: String)
  case class Entities(content: Seq[String])

  def spec: Spec[Any, Throwable] = suite("KeyValueMVStore")(
    test("put and get") {
      for {
        mvStore <- ZIO.service[MVStore]
        kvStore <- KeyValueMVStore
          .makeMVStoreKv[Int, Entity](mvStore, "test-entity")
        _ <- kvStore.put(1, Entity("Hello"))
        entity <- kvStore.get(1)
      } yield assertTrue(
        entity.isDefined,
        entity.get.content == "Hello"
      )
    }.provide(ZLayer.scoped(KeyValueMVStore.makeMVStoreMemory())),
    test("put and get list") {
      for {
        mvStore <- ZIO.service[MVStore]
        kvStore <- KeyValueMVStore.makeMVStoreKv[Int, Entities](
          mvStore,
          "test-entity"
        )
        _ <- kvStore.put(1, Entities(Seq("Hello", "World")))
        entity <- kvStore.get(1)
      } yield assertTrue(
        entity.isDefined,
        entity.get.content == Seq("Hello", "World")
      )
    }.provide(ZLayer.scoped(KeyValueMVStore.makeMVStoreMemory()))
  )
}
