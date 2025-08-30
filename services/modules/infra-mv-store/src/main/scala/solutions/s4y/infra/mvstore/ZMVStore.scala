package solutions.s4y.infra.mvstore

import org.h2.mvstore.MVStore
import zio.{Cause, Scope, ZIO}

object ZMVStore:
  private def makeMvStore(
      file: Option[String]
  ): ZIO[Scope, Throwable, MVStore] =
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

end ZMVStore
