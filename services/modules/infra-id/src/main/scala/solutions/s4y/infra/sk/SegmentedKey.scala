package solutions.s4y.infra.sk

import java.util.UUID

trait ToSegment[T]:
  def apply(value: T): String

class SegmentedKey(val id: String):
  def ::[T]: SegmentedKey =
    SegmentedKey(s"$id${SegmentedKey.delimiter}")

  def ::[T](value: T)(using toSegment: ToSegment[T]): SegmentedKey =
    SegmentedKey(s"${toSegment(value)}${SegmentedKey.delimiter}$id")

private object SegmentedKey:
  private[infra] val delimiter: String = ":"

  given [T: ToSegment]: Conversion[T, SegmentedKey] with
    def apply(segmented: T): SegmentedKey = new SegmentedKey(
      summon[ToSegment[T]](segmented)
    )

  given Conversion[SegmentedKey, String] with
    def apply(value: SegmentedKey): String = value.id

  given ToSegment[String] with
    def apply(value: String): String = value

  given ToSegment[Int] with
    def apply(value: Int): String = value.toString

  given ToSegment[Long] with
    def apply(value: Long): String = value.toString

  given ToSegment[UUID] with
    def apply(value: UUID): String = value.toString
