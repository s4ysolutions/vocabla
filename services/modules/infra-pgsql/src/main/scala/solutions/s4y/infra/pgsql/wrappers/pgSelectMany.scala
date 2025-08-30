package solutions.s4y.infra.pgsql.wrappers

import solutions.s4y.i18n.ResourcesStringsResolver.default
import solutions.s4y.i18n.t
import solutions.s4y.infra.pgsql.tx.TransactionContextPg
import solutions.s4y.vocabla.app.repo.error.InfraFailure
import solutions.s4y.vocabla.app.repo.error.InfraFailure.mapThrowable
import zio.{Chunk, ZIO}

import scala.util.Using

def pgSelectMany[R, A](
    sql: String,
    setParams: java.sql.PreparedStatement => Unit,
    mapResult: java.sql.ResultSet => A
)(using ctx: TransactionContextPg): ZIO[R, InfraFailure, Chunk[A]] =
  ZIO.scoped {
    ZIO
      .fromAutoCloseable(
        ZIO
          .attempt(ctx.connection.prepareStatement(sql))
          .mapThrowable(t"""Failed to prepare statement "$sql"""")
      )
      .flatMap { st =>
        ZIO
          .attempt {
            setParams(st)
            Using.resource(st.executeQuery()) { rs =>
              val builder = Chunk.newBuilder[A]
              while (rs.next()) {
                builder += mapResult(rs)
              }
              builder.result()
            }
          }
          .mapThrowable(t"""Failed to execute query: "$sql"""")
      }
  }

def pgSelectMany[R, A](
    sql: String,
    mapResult: java.sql.ResultSet => A
)(using TransactionContextPg): ZIO[R, InfraFailure, Chunk[A]] =
  pgSelectMany[R, A](sql, _ => (), mapResult)
/*
def pgSelectMany[R, A](
    ctx: TransactionContextPg,
    sql: String,
    mapResult: java.sql.ResultSet => A
): ZIO[TransactionContextPg & R, InfraFailure, Chunk[A]] =
  pgSelectMany(ctx, sql, _ => (), mapResult)
 */
