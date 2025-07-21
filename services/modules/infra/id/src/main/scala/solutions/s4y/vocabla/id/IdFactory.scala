package solutions.s4y.vocabla.id

import zio.{Random, UIO}

import java.util.UUID

trait IdFactory[ID]:
  self =>

  def next: UIO[ID]

  def map[ID2](f: ID => ID2): IdFactory[ID2] =
    new IdFactory[ID2]:
      override def next: UIO[ID2] = self.next.map(f)

object IdFactory:
  def uuid: IdFactory[UUID] = new IdFactory[UUID]:
    override def next: UIO[UUID] = Random.nextUUID

  def string: IdFactory[String] = new IdFactory[String]:
    override def next: UIO[String] = Random.nextUUID.map(_.toString)

  def long: IdFactory[Long] = new IdFactory[Long]:
    override def next: UIO[Long] = Random.nextLong
