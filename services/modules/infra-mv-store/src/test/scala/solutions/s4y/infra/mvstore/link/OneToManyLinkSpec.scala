package solutions.s4y.infra.mvstore.link

import solutions.s4y.infra.mvstore.ZMVStore.makeMVStoreMemory
import solutions.s4y.infra.sk.SegmentedKey.given
import solutions.s4y.zio.consoleColorDebugLogger
import zio.*
import zio.test.*
import zio.test.Assertion.*

object OneToManyLinkSpec extends ZIOSpecDefault {
  override def spec = suite("OneToManyLink")(
    test("associate and getRights") {
      for {
        link <- OneToManyLink.make[String, String]("test")
        _ <- link.associate("parent1", "child1")
        _ <- link.associate("parent1", "child2")
        _ <- link.associate("parent2", "child3")
        children1 <- link.getRights("parent1").runCollect
        children2 <- link.getRights("parent2").runCollect
      } yield assert(children1)(
        contains("child1") && contains("child2")
      ) && assert(children1)(contains("child2"))
    },
    test("disassociate removes child") {
      for {
        link <- OneToManyLink.make[String, String]("test")
        _ <- link.associate("parent1", "child1")
        _ <- link.associate("parent1", "child2")
        _ <- link.associate("parent2", "child3")
        _ <- link.disassociate("parent1", "child1")
        children1 <- link.getRights("parent1").runCollect
        children2 <- link.getRights("parent2").runCollect
      } yield assert(children1)(hasSameElements(Seq("child2"))) &&
        assert(children2)(hasSameElements(Seq("child3")))
    }
  ).provide(ZLayer.scoped(makeMVStoreMemory()) ++ consoleColorDebugLogger)
}
