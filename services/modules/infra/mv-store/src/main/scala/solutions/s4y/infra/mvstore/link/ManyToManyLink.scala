package solutions.s4y.infra.mvstore.link
import org.h2.mvstore.MVStore
import solutions.s4y.infra.mvstore.ZMVMap
import solutions.s4y.infra.sk.SegmentedKey.given
import solutions.s4y.infra.sk.ToSegment
import zio.ZIO
import zio.stream.ZStream

import scala.language.postfixOps

final class ManyToManyLink[LID: ToSegment, RID: ToSegment](
    val linkLeft: ZMVMap[String, LID],
    val linkRight: ZMVMap[String, RID]
):
  def associate(leftId: LID, rightId: RID): ZIO[Any, String, Unit] =
    (linkLeft.put(rightId :: leftId, leftId) <&>
      linkRight.put(leftId :: rightId, rightId)).unit

  def disassociate(leftId: LID, rightId: RID): ZIO[Any, String, Unit] =
    (linkLeft.remove(rightId :: leftId) <&>
      linkRight.remove(leftId :: rightId)).unit

  def getLefts(rightId: RID): ZStream[Any, String, LID] =
    linkLeft.cursorOfPrefix(rightId ::)

  def getRights(leftId: LID): ZStream[Any, String, RID] =
    linkRight.cursorOfPrefix(leftId ::)
end ManyToManyLink

object ManyToManyLink:
  def make[LID: ToSegment, RID: ToSegment](
      name: String
  ): ZIO[MVStore, String, ManyToManyLink[LID, RID]] =
    for {
      left <- ZMVMap.make[String, LID](name + "_left")
      right <- ZMVMap.make[String, RID](name + "_right")
    } yield new ManyToManyLink(left, right)
end ManyToManyLink
