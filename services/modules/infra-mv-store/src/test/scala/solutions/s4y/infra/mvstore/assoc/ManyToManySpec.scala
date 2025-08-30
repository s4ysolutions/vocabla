package solutions.s4y.infra.mvstore.assoc

import org.h2.mvstore.MVStore
import solutions.s4y.infra.mvstore.ZMVMap
import solutions.s4y.infra.mvstore.ZMVStore.makeMVStoreMemory
import solutions.s4y.infra.sk.SegmentedKey.given
import solutions.s4y.zio.consoleColorDebugLogger
import zio.*
import zio.test.*
import zio.test.Assertion.*

import scala.jdk.CollectionConverters.given

object ManyToManySpec extends ZIOSpecDefault {
  def spec = suite("ManyToMany")(
    test("associate and retrieve") {
      for {
        leftMap <- ZMVMap.make[Int, String]("left")
        rightMap <- ZMVMap.make[String, Int]("right")
        assoc <- ManyToMany.make(leftMap, rightMap, "assoc")
        _ <- leftMap.put(1, "A")
        _ <- rightMap.put("B", 2)
        _ <- assoc.associate(1, "B")
        rights <- assoc.getRights(1).runCollect
        lefts <- assoc.getLefts("B").runCollect
      } yield {
        assert(assoc.link.linkRight.map.entrySet().asScala)(
          hasSize(equalTo(1))
        ) &&
        assert(assoc.link.linkLeft.map.entrySet().asScala)(
          hasSize(equalTo(1))
        ) &&
        assert(rights)(hasSize(equalTo(1))) &&
        assert(lefts)(hasSize(equalTo(1))) &&
        assert(rights)(contains(2))
        assert(lefts)(contains("A"))
      }
    },
    test("disassociate") {
      for {
        leftMap <- ZMVMap.make[Int, String]("left")
        rightMap <- ZMVMap.make[String, Int]("right")
        assoc <- ManyToMany.make(leftMap, rightMap, "assoc")
        _ <- leftMap.put(1, "A")
        _ <- rightMap.put("B", 2)
        _ <- assoc.associate(1, "B")
        _ <- assoc.disassociate(1, "B")
        rights <- assoc.getRights(1).runCollect
        lefts <- assoc.getLefts("B").runCollect
      } yield assertTrue(rights.isEmpty) && assertTrue(lefts.isEmpty)
    }
  ).provide(ZLayer.scoped(makeMVStoreMemory()) ++ consoleColorDebugLogger)
}
