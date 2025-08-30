package solutions.s4y.infra.mvstore.assoc

import org.h2.mvstore.MVStore
import solutions.s4y.infra.mvstore.ZMVMap
import solutions.s4y.infra.mvstore.link.ManyToManyLink
import solutions.s4y.infra.sk.ToSegment
import zio.stream.ZStream
import zio.{IO, ZIO}

class ManyToMany[LID, LV, RID, RV](
    val left: ZMVMap[LID, LV],
    val right: ZMVMap[RID, RV],
    val link: ManyToManyLink[LID, RID]
):
  def associate(leftId: LID, rightId: RID): IO[String, Unit] =
    link.associate(leftId, rightId)

  def disassociate(leftId: LID, rightId: RID): IO[String, Unit] =
    link.disassociate(leftId, rightId)

  def getRights(leftId: LID): ZStream[Any, String, RV] =
    link.getRights(leftId).mapZIO { rightId => right.get(rightId) }.collectSome

  def getLefts(rightId: RID): ZStream[Any, String, LV] =
    link.getLefts(rightId).mapZIO { leftId => left.get(leftId) }.collectSome

  def deleteRighsts(leftId: LID): IO[String, Unit] =
    link
      .getRights(leftId)
      .mapZIO(rightId =>
        link.disassociate(leftId, rightId) <&> right.remove(rightId)
      )
      .runDrain

  def deleteLefts(rightId: RID): IO[String, Unit] =
    link
      .getLefts(rightId)
      .mapZIO(leftId =>
        link.disassociate(leftId, rightId) <&> left.remove(leftId)
      )
      .runDrain
end ManyToMany

object ManyToMany:
  def make[LID: ToSegment, LV, RID: ToSegment, RV](
      left: ZMVMap[LID, LV],
      right: ZMVMap[RID, RV],
      name: String
  ): ZIO[MVStore, String, ManyToMany[LID, LV, RID, RV]] =
    ManyToManyLink
      .make[LID, RID](name)
      .map(
        new ManyToMany(left, right, _)
      )
