package solutions.s4y.vocabla.infrastructure.mvstore

import org.h2.mvstore.MVMap
import zio.IO
import zio.stream.ZStream

class SkZMVMap[V](private val map: ZMVMap[String, V]) {

  def get(key: SegmentedKey): IO[String, Option[V]] = map.get(key.id)
  def put(key: SegmentedKey, value: V): IO[String, V] = map.put(key.id, value)

  def cursor(
      from: SegmentedKey,
      to: SegmentedKey,
      inclusive: Boolean = true
  ): ZStream[Any, String, (String, V)] =
    map.cursor(from.id, to.id, inclusive)

  def cursorOf(prefix: SegmentedKey): ZStream[Any, String, V] =
    map.cursorOf(prefix.id + SegmentedKey.delimiter)
}

object SkZMVMap:
  def apply[V](map: MVMap[String, V]): SkZMVMap[V] = new SkZMVMap(ZMVMap(map))
