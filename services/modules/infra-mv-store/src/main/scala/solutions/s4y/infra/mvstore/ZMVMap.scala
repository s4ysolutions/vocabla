package solutions.s4y.infra.mvstore

import org.h2.mvstore.{Cursor, MVMap, MVStore}
import solutions.s4y.zio.e
import zio.stream.ZStream
import zio.{IO, ZIO}

final class ZMVMap[K, V](val map: MVMap[K, V]) {

  def get(key: K): IO[String, Option[V]] = {
    ZIO.logTrace(s"Retrieving key: ${map.getName} [$key]") *>
      ZIO
        .attempt(Option(map.get(key)))
        .tap(value =>
          ZIO.logTrace(s"Retrieved ${map.getName} [$key]: [$key]=$value")
        )
        .e(e =>
          s"Error retrieving key: ${map.getName} [$key], error: ${e.getMessage}"
        )
  }

  def put(key: K, value: V): IO[String, V] =
    ZIO.logTrace(s"Putting ${map.getName} key: [$key | value: $value]") *>
      ZIO
        .attempt(map.put(key, value))
        .e(e =>
          s"Error putting ${map.getName} key: $key, value: $value, error: ${e.getMessage}"
        )

  def remove(key: K): IO[String, Option[V]] =
    ZIO.logTrace(s"Removing ${map.getName} key: $key") *>
      ZIO
        .attempt(Option(map.remove(key)))
        .e(e =>
          s"Error removing ${map.getName} key: $key, error: ${e.getMessage}"
        )

  def cursor(
      from: K,
      to: K,
      reverse: Boolean = true
  ): ZStream[Any, String, (K, V)] =
    ZMVMap.cursorToStream(map.cursor(from, to, reverse))

  def cursor(from: K): ZStream[Any, String, (K, V)] =
    ZMVMap.cursorToStream(map.cursor(from))

  def cursorOf(key: K): ZStream[Any, String, V] = {
    cursor(key)
      .takeWhile((k, _) => key == k)
      .map((key, value) => value)
  }

  def cursorOfPrefix(prefix: K): ZStream[Any, String, V] =
    cursor(prefix)
      .takeWhile((key, _) => key.toString.startsWith(prefix.toString))
      .map((key, value) => value)
}

object ZMVMap:
  def apply[K, V](map: MVMap[K, V]): ZMVMap[K, V] = new ZMVMap(map)
  def make[K, V](name: String): ZIO[MVStore, String, ZMVMap[K, V]] =
    ZIO.serviceWithZIO[MVStore](mvStore =>
      ZIO
        .attempt(new ZMVMap(mvStore.openMap(name)))
        .e(e => s"Failed to create ZMVMap for $name: ${e.getMessage}")
    )

  private def cursorToStream[K, V](
      cursor: Cursor[K, V]
  ): ZStream[Any, String, (K, V)] =
    ZStream
      .unfold(cursor) { cursor =>
        if (cursor.hasNext) {
          val key: K = cursor.next()
          val value: V = cursor.getValue
          Some(((key, value)), cursor)
        } else None
      }
end ZMVMap
