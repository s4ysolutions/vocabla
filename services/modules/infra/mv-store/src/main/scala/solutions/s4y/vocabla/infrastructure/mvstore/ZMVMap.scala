package solutions.s4y.vocabla.infrastructure.mvstore

import org.h2.mvstore.{Cursor, MVMap}
import solutions.s4y.vocabla.error.e
import zio.stream.ZStream
import zio.{IO, ZIO}

class ZMVMap[K, V](private val map: MVMap[K, V]) {

  def get(key: K): IO[String, Option[V]] = {
    ZIO.logTrace("Retrieving key: " + key) *>
      ZIO
        .attempt(Option(map.get(key)))
        .tap(value => ZIO.logTrace(s"Retrieved [$key]=$value"))
        .e(e => s"Error retrieving key: $key, error: ${e.getMessage}")
  }

  def put(key: K, value: V): IO[String, V] =
    ZIO.logTrace(s"Putting key: $key, value: $value") *>
      ZIO
        .attempt(map.put(key, value))
        .e(e =>
          s"Error putting key: $key, value: $value, error: ${e.getMessage}"
        )

  def remove(key: K): IO[String, Option[V]] =
    ZIO.logTrace(s"Removing key: $key") *>
      ZIO
        .attempt(Option(map.remove(key)))
        .e(e => s"Error removing key: $key, error: ${e.getMessage}")

  def cursor(
      from: K,
      to: K,
      reverse: Boolean = true
  ): ZStream[Any, String, (K, V)] =
    ZMVMap.cursorToStream(map.cursor(from, to, reverse))

  def cursor(from: K): ZStream[Any, String, (K, V)] =
    ZMVMap.cursorToStream(map.cursor(from))

  def cursorOf(prefix: K): ZStream[Any, String, V] =
    cursor(prefix)
      .takeWhile((key, _) => key.toString.startsWith(prefix.toString))
      .map(_._2)
}

object ZMVMap:
  def apply[K, V](map: MVMap[K, V]): ZMVMap[K, V] = new ZMVMap(map)

  def cursorToStream[K, V](cursor: Cursor[K, V]): ZStream[Any, String, (K, V)] =
    ZStream
      .unfold(cursor) { cursor => cursorNextS(cursor) }

  private def cursorNext[K, V](cursor: Cursor[K, V]): Option[(K, V)] =
    if (cursor.hasNext) {
      val key: K = cursor.next()
      val value: V = cursor.getValue
      Some((key, value))
    } else None

  private def cursorNextS[K, V](
      cursor: Cursor[K, V]
  ): Option[((K, V), Cursor[K, V])] =
    cursorNext(cursor).map((_, cursor))

end ZMVMap
