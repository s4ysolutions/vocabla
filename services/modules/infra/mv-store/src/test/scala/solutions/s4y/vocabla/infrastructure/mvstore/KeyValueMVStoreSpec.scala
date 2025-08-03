package solutions.s4y.vocabla.infrastructure.mvstore

import solutions.s4y.vocabla.infrastructure.mvstore.KeyValueMVStore.makeMVStoreMemory
import solutions.s4y.vocabla.infrastructure.mvstore.KeyValueMVStoreSpec.test
import solutions.s4y.vocabla.infrastructure.mvstore.SegmentedKey.given
import solutions.s4y.vocabla.logging.consoleColorTraceLogger
import zio.test.{Assertion, Spec, TestEnvironment, ZIOSpecDefault, assert}
import zio.{Chunk, IO, Scope, ZIO, ZLayer}

object KeyValueMVStoreSpec extends ZIOSpecDefault:

  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("KeyValueMVStoreSpec")(
      suite("Segmented key")(
        test("sk can be created from a single value") {
          val sut: SegmentedKey = 10
          assert(sut.id)(Assertion.equalTo("10"))
        },
        test("sk can be created from a combined value") {
          val sut = 10 :: "another"
          assert(sut.id)(Assertion.equalTo("10:another"))
        },
        test("sk can be used as a key") {
          val sk1 = "sk1" :: "sk2" :: "sk3"
          val value1 = "value1"
          for {
            map <- ZIO.service[ZMVMap[String, String]]
            _ <- map.put(sk1.id, value1)
            value <- map.get(sk1.id)
          } yield assert(value)(Assertion.equalTo(Some(value1)))
        }
      ),
      suite("cursor")(
        test("sk can be used as a filter") {
          for {
            map <- ZIO.service[ZMVMap[String, String]]
            _ <- populateMap(map)
            cursor = map.cursor(1 :: 2, 1 :: 3, false)
            read <- cursor.runCollect
          } yield assert(read)(
            Assertion.equalTo(
              Chunk(
                ("1:2:1", "121"),
                ("1:2:2", "122"),
                ("1:2:3", "123")
              )
            )
          ) && assert(read.size)(Assertion.equalTo(3))
        },
        test("sk can be used as a empty filter") {
          for {
            map <- ZIO.service[ZMVMap[String, String]]
            _ <- populateMap(map)
            cursor = map.cursor(4 :: 1)
            read <- cursor.runCollect
          } yield assert(read)(Assertion.isEmpty)
        },
        test("sk can be used as a prefix filter") {
          for {
            map <- ZIO.service[ZMVMap[String, String]]
            _ <- populateMap(map)
            cursor = map.cursorOf(1 :: 2)
            read <- cursor.runCollect
          } yield assert(read)(
            Assertion.equalTo(
              Chunk("121", "122", "123")
            )
          )
        }
      )
    ).provide(
      ZLayer.scoped(
        makeMVStoreMemory().map(store =>
          ZMVMap(store.openMap[String, String]("sk"))
        )
      ) ++ consoleColorTraceLogger
    )
  }
end KeyValueMVStoreSpec

private def populateMap(
    map: ZMVMap[String, String]
): IO[String, Unit] = for {
  _ <- map.put(1 :: 1 :: 1, "111")
  _ <- map.put(1 :: 1 :: 2, "112")
  _ <- map.put(1 :: 1 :: 3, "113")
  _ <- map.put(1 :: 2 :: 1, "121")
  _ <- map.put(1 :: 2 :: 2, "122")
  _ <- map.put(1 :: 2 :: 3, "123")
  _ <- map.put(1 :: 3 :: 1, "131")
  _ <- map.put(1 :: 3 :: 2, "132")
  _ <- map.put(1 :: 3 :: 3, "133")
} yield ()
