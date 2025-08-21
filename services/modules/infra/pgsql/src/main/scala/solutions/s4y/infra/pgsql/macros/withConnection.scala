package solutions.s4y.infra.pgsql.macros

import solutions.s4y.infra.pgsql.tx.TransactionContextPg
import solutions.s4y.vocabla.app.repo.tx.TransactionContext
import zio.ZIO

import java.sql.Connection
import scala.quoted.*

/** Macro that accepts any expression that can access connection Usage:
  * withConnection { conn => /* your code using conn */ }
  */
inline def withConnection[R, E, A](
    inline f: Connection => ZIO[R, E, A]
): ZIO[R & TransactionContext, E, A] =
  ${ withConnectionImpl('f) }

private def withConnectionImpl[R: Type, E: Type, A: Type](
    f: Expr[Connection => ZIO[R, E, A]]
)(using Quotes): Expr[ZIO[R & TransactionContext, E, A]] = {
  '{
    ZIO
      .serviceWithZIO[TransactionContext] { tx =>
        val connection = tx.asInstanceOf[TransactionContextPg].connection
        $f(connection)
      }
  }
}
