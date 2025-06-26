package solutions.s4y.vocabla.infrastructure.mvstore

import org.h2.mvstore.{MVMap, MVStore}
import solutions.s4y.vocabla.infrastructure.kv.KeyValue
import zio.{Cause, IO, Scope, Task, ZIO}

class KeyValueMVStore[Key, E](map: MVMap[Key, E]) extends KeyValue[Key, E] {
  type DTO

  override def put(
      key: Key,
      e: E
  ): Task[Key] = ZIO.succeed {
    map.put(key, e)
    key
  }

  override def get(key: Key): Task[Option[E]] = ZIO.succeed {
    Option(map.get(key))
  }

  override def delete(key: Key): Task[Unit] = ZIO.succeed {
    map.remove(key)
  }
  /*
  override def stream: ZStream[Any, Throwable, E] =
    ZStream
      .fromJavaIterator[E](
        map.values().iterator()
      )*/
}

object KeyValueMVStore {
  private def makeMvStore(file: Option[String]): ZIO[Scope, Throwable, MVStore] =
    ZIO.acquireRelease(
      ZIO.blocking {
        ZIO
          .attempt {
            val builder = new MVStore.Builder()
            file match {
              case Some(f) => builder.fileName(f).open()
              case None    => builder.open()
            }
          }
          .tapError(e => ZIO.logWarningCause(e.getMessage, Cause.fail(e)))
      }
    )(store =>
      ZIO.blocking {
        ZIO.attempt(store.close()).orDie
      }
    )
  def makeMVStoreMemory(): ZIO[Scope, Throwable, MVStore] = makeMvStore(None)

  def makeMVStoreKv[Key, E](
      mvStore: MVStore,
      mapName: String
  ): IO[Throwable, KeyValue[Key, E]] = ZIO.attempt(
    new KeyValueMVStore[Key, E](
      mvStore.openMap[Key, E](mapName)
    )
  )
}
