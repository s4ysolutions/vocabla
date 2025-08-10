package solutions.s4y.infra.mvstore.link

import org.h2.mvstore.MVStore
import solutions.s4y.infra.mvstore.ZMVMap
import solutions.s4y.infra.sk.SegmentedKey.given
import solutions.s4y.infra.sk.ToSegment
import zio.ZIO
import zio.stream.ZStream

import scala.language.postfixOps

final class OneToManyLink[ParentID: ToSegment, ChildrenID: ToSegment](
    val linkChild: ZMVMap[String, ChildrenID]
):
  def associate(leftId: ParentID, rightId: ChildrenID): ZIO[Any, String, Unit] =
    linkChild.put(leftId :: rightId, rightId).unit

  def disassociate(
      leftId: ParentID,
      rightId: ChildrenID
  ): ZIO[Any, String, Unit] =
    linkChild.remove(leftId :: rightId).unit

  def getRights(leftId: ParentID): ZStream[Any, String, ChildrenID] =
    linkChild.cursorOfPrefix(leftId ::)
end OneToManyLink

object OneToManyLink:
  def make[LID: ToSegment, RID: ToSegment](
      name: String
  ): ZIO[MVStore, String, OneToManyLink[LID, RID]] =
    ZMVMap
      .make[String, RID](name + "_children")
      .map { new OneToManyLink(_) }
end OneToManyLink
