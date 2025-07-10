package solutions.s4y.vocabla.id

import zio.{Random, UIO}

trait IdFactory[ID]:
  self =>

  def next: UIO[ID]

  def map[ID2](f: ID => ID2): IdFactory[ID2] =
    new IdFactory[ID2]:
      override def next: UIO[ID2] = self.next.map(f)

object IdFactory:
  def uuid: IdFactory[String] = new IdFactory[String]:
    override def next: UIO[String] = Random.nextUUID.map(_.toString)

  def long: IdFactory[Long] = new IdFactory[Long]:
    override def next: UIO[Long] = Random.nextLong

  given IdFactory[String] = uuid
  given IdFactory[Long] = long
