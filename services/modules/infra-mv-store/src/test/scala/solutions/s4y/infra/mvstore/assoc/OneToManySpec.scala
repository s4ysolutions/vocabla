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

object OneToManySpec extends ZIOSpecDefault {
  def spec = suite("OneToMany")(
    test("associate and getChildren") {
      for {
        parentMap <- ZMVMap.make[Int, String]("parent")
        childMap <- ZMVMap.make[String, Int]("child")
        assoc <- OneToMany.make(parentMap, childMap, "assoc")
        _ <- parentMap.put(1, "A")
        _ <- childMap.put("B", 2)
        _ <- assoc.associate(1, "B")
        children <- assoc.getChildren(1).runCollect
      } yield {
        assert(children)(hasSize(equalTo(1))) &&
        assert(children)(contains(("B", 2)))
      }
    },
    test("disassociate removes child from parent") {
      for {
        parentMap <- ZMVMap.make[Int, String]("parent")
        childMap <- ZMVMap.make[String, Int]("child")
        assoc <- OneToMany.make(parentMap, childMap, "assoc")
        _ <- parentMap.put(1, "A")
        _ <- childMap.put("B", 2)
        _ <- assoc.associate(1, "B")
        _ <- assoc.disassociate(1, "B")
        children <- assoc.getChildren(1).runCollect
      } yield assertTrue(children.isEmpty)
    },
    test("deleteChildren removes children and links") {
      for {
        parentMap <- ZMVMap.make[Int, String]("parent")
        childMap <- ZMVMap.make[String, Int]("child")
        assoc <- OneToMany.make(parentMap, childMap, "assoc")
        _ <- parentMap.put(1, "A")
        _ <- childMap.put("B", 2)
        _ <- assoc.associate(1, "B")
        _ <- assoc.deleteChildren(1)
        children <- assoc.getChildren(1).runCollect
        childValue <- childMap.get("B")
      } yield assertTrue(children.isEmpty) && assertTrue(childValue.isEmpty)
    }
  ).provide(ZLayer.scoped(makeMVStoreMemory()) ++ consoleColorDebugLogger)
}
