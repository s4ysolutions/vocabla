package solutions.s4y.vocabla.id

import zio.{Random, UIO}

trait IdFactory[ID]:
  def next: UIO[ID]

object IdFactory:
  def uuid: IdFactory[String] = new IdFactory[String]:
    override def next: UIO[String] = Random.nextUUID.map(_.toString)

  def long: IdFactory[Long] = new IdFactory[Long]:
    override def next: UIO[Long] = Random.nextLong
