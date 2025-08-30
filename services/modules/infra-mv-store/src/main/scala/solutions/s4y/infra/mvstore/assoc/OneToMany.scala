package solutions.s4y.infra.mvstore.assoc

import org.h2.mvstore.MVStore
import solutions.s4y.infra.mvstore.ZMVMap
import solutions.s4y.infra.mvstore.link.ManyToManyLink
import solutions.s4y.infra.sk.ToSegment
import zio.stream.ZStream
import zio.{IO, ZIO}

class OneToMany[PID, PV, CID, CV](
    val parent: ZMVMap[PID, PV],
    val child: ZMVMap[CID, CV],
    val link: ManyToManyLink[PID, CID]
):
  def associate(parentId: PID, childId: CID): IO[String, Unit] =
    link.associate(parentId, childId)

  def disassociate(parentId: PID, childId: CID): IO[String, Unit] =
    link.disassociate(parentId, childId)

  def getChildren(parentId: PID): ZStream[Any, String, (CID, CV)] =
    link
      .getRights(parentId)
      .mapZIO(childId =>
        child
          .get(childId)
          .map {
            case Some(cv) => Some((childId, cv))
            case None     => None
          }
      )
      .collectSome

  def getParents(childId: CID): ZStream[Any, String, (PID, PV)] =
    link
      .getLefts(childId)
      .mapZIO(parentId =>
        parent
          .get(parentId)
          .map {
            case Some(pv) => Some((parentId, pv))
            case None     => None
          }
      )
      .collectSome

  def deleteChildren(parentId: PID): IO[String, Unit] =
    link
      .getRights(parentId)
      .mapZIO(childId =>
        link.disassociate(parentId, childId) <&> child.remove(childId)
      )
      .runDrain

  def deleteParents(childId: CID): IO[String, Unit] =
    link
      .getLefts(childId)
      .mapZIO(parentId =>
        link.disassociate(parentId, childId) <&> parent.remove(parentId)
      )
      .runDrain
end OneToMany

object OneToMany:
  def make[PID: ToSegment, PV, CID: ToSegment, CV](
      parent: ZMVMap[PID, PV],
      child: ZMVMap[CID, CV],
      name: String
  ): ZIO[MVStore, String, OneToMany[PID, PV, CID, CV]] =
    ManyToManyLink.make[PID, CID](name).map(new OneToMany(parent, child, _))
end OneToMany
