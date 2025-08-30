package solutions.s4y.infra.mvstore.assoc

import org.h2.mvstore.MVStore
import solutions.s4y.infra.mvstore.ZMVMap
import solutions.s4y.infra.mvstore.link.ManyToManyLink
import solutions.s4y.infra.sk.ToSegment
import zio.stream.ZStream
import zio.{IO, ZIO}

class IdToMany[PID, CID, CV](
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
        child.get(childId).map {
          case Some(cv) => Some((childId, cv))
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
end IdToMany

object IdToMany:
  def make[PID: ToSegment, CID: ToSegment, CV](
      child: ZMVMap[CID, CV],
      name: String
  ): ZIO[MVStore, String, IdToMany[PID, CID, CV]] =
    ManyToManyLink.make[PID, CID](name).map(new IdToMany(child, _))
end IdToMany
