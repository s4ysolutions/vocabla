package solutions.s4y.infra.mvstore.link

import org.h2.mvstore.MVStore
import solutions.s4y.infra.mvstore.ZMVStore.makeMVStoreMemory
import solutions.s4y.infra.sk.SegmentedKey.given
import solutions.s4y.zio.consoleColorDebugLogger
import zio.*
import zio.test.Assertion.*
import zio.test.{Assertion, Spec, ZIOSpecDefault, assert}

import java.util.AbstractMap.SimpleEntry
import java.util.Map.Entry
import scala.collection.mutable
import scala.jdk.CollectionConverters.given

object ManyToManyLinkSpec extends ZIOSpecDefault {
  def spec =
    suite("ManyToManyLink")(
      test("associate and retrieve links") {
        for {
          link <- ManyToManyLink.make[Int, String]("test")
          _ <- link.associate(1, "A")
          _ <- link.associate(1, "B")
          rights <- link.getRights(1).runCollect
          leftsA <- link.getLefts("A").runCollect
          leftsB <- link.getLefts("B").runCollect
          li: mutable.Iterable[Entry[String, Int]] = link.linkLeft.map
            .entrySet()
            .asScala
          ri: mutable.Iterable[Entry[String, String]] = link.linkRight.map
            .entrySet()
            .asScala
        } yield assert(li)(Assertion.hasSize(equalTo(2))) &&
          assert(ri)(Assertion.hasSize(equalTo(2))) &&
          assert(li)(
            Assertion.hasSameElements[Entry[String, Int]](
              Chunk[Entry[String, Int]](
                new SimpleEntry("A:1", 1),
                new SimpleEntry("B:1", 1)
              )
            )
          ) &&
          assert(ri)(
            Assertion.hasSameElements[Entry[String, String]](
              Chunk[Entry[String, String]](
                new SimpleEntry("1:A", "A"),
                new SimpleEntry("1:B", "B")
              )
            )
          ) &&
          assert(rights)(Assertion.hasSize(equalTo(2))) &&
          assert(leftsA)(Assertion.hasSize(equalTo(1))) &&
          assert(leftsB)(Assertion.hasSize(equalTo(1))) &&
          assert(rights)(Assertion.hasSameElements(Seq("A", "B"))) &&
          assert(leftsA)(Assertion.contains(1)) &&
          assert(leftsB)(Assertion.contains(1))
      },
      test("disassociate links") {
        for {
          link <- ManyToManyLink.make[Int, String]("test")
          _ <- link.associate(1, "A")
          _ <- link.associate(1, "B")
          _ <- link.disassociate(1, "A")
          rights <- link.getRights(1).runCollect
          leftsA <- link.getLefts("A").runCollect
          leftsB <- link.getLefts("B").runCollect
          li = link.linkLeft.map
            .entrySet()
            .asScala
          ri = link.linkRight.map
            .entrySet()
            .asScala
        } yield assert(li)(Assertion.hasSize(equalTo(1))) &&
          assert(ri)(Assertion.hasSize(equalTo(1))) &&
          assert(ri)(
            Assertion.hasSameElements[Entry[String, String]](
              Chunk[Entry[String, String]](
                new SimpleEntry("1:B", "B")
              )
            )
          ) &&
          assert(li)(
            Assertion.hasSameElements[Entry[String, Int]](
              Chunk[Entry[String, Int]](
                new SimpleEntry("B:1", 1)
              )
            )
          ) &&
          assert(rights)(Assertion.hasSize(equalTo(1))) &&
          assert(leftsA)(Assertion.hasSize(equalTo(0))) &&
          assert(leftsB)(Assertion.hasSize(equalTo(1))) &&
          assert(rights)(Assertion.hasSameElements(Seq("B"))) &&
          assert(leftsB)(Assertion.contains(1))
      }
    ).provide(ZLayer.scoped(makeMVStoreMemory()) ++ consoleColorDebugLogger)
}
