package solutions.s4y.infra.pgsql.wrappers

import solutions.s4y.infra.pgsql.tx.TransactionContextPg
import zio.ZIO

import java.sql.Connection
import scala.language.postfixOps

def pgWithConnection[E, A](
    zio: Connection => ZIO[TransactionContextPg, E, A]
): ZIO[TransactionContextPg, E, A] =
  ZIO.serviceWithZIO[TransactionContextPg] { tx =>
    val connection = tx.connection
    zio(connection)
  }
