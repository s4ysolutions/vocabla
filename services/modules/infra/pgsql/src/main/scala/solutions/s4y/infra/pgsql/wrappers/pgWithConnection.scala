package solutions.s4y.infra.pgsql.wrappers

import solutions.s4y.infra.pgsql.tx.TransactionContextPg
import solutions.s4y.vocabla.app.repo.tx.TransactionContext
import zio.ZIO

import java.sql.Connection

def pgWithConnection[R, E, A](
    f: Connection => ZIO[R, E, A]
): ZIO[R & TransactionContext, E, A] =
  ZIO.serviceWithZIO[TransactionContext] { tx =>
    val connection = tx.asInstanceOf[TransactionContextPg].connection
    f(connection)
  }
