package solutions.s4y.infra.pgsql.wrappers

import solutions.s4y.i18n.ResourcesStringsResolver.default
import solutions.s4y.i18n.t
import solutions.s4y.infra.pgsql.tx.TransactionContextPg
import solutions.s4y.vocabla.app.repo.error.InfraFailure
import solutions.s4y.vocabla.app.repo.error.InfraFailure.mapThrowable
import zio.ZIO

import java.sql.PreparedStatement

def pgUpdateBatch[R](
    sql: String,
    setParams: PreparedStatement => Unit
)(using ctx: TransactionContextPg): ZIO[R, InfraFailure, Array[Int]] =
  ZIO.scoped {
    for {
      st <- ZIO
        .fromAutoCloseable(
          ZIO.attempt(
            ctx.connection.prepareStatement(sql)
          )
        )
        .mapThrowable(t"""Failed to prepare statement: "$sql"""")
      _ <- ZIO
        .attempt(setParams(st))
        .mapThrowable(t"""Failed to set parameters for: "$sql"""")
      rowsAffected <- ZIO
        .attempt(st.executeBatch())
        .mapThrowable(t"""Failed to execute statement: "$sql"""")
    } yield rowsAffected
  }
