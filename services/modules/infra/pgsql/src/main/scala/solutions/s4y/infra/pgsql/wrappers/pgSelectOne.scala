package solutions.s4y.infra.pgsql.wrappers

import solutions.s4y.i18n.ResourcesStringsResolver.default
import solutions.s4y.i18n.t
import solutions.s4y.infra.pgsql.tx.TransactionContextPg
import solutions.s4y.vocabla.app.repo.error.InfraFailure
import zio.ZIO

def pgSelectOne[R, A](
    sql: String,
    setParams: java.sql.PreparedStatement => Unit,
    mapResult: java.sql.ResultSet => A
)(using TransactionContextPg): ZIO[R, InfraFailure, Option[A]] =
  pgSelectMany(sql, setParams, mapResult).flatMap { results =>
    if results.size == 1 then ZIO.some(results.head)
    else if results.isEmpty then ZIO.none
    else
      ZIO.fail(
        InfraFailure(
          t"""Expected exactly one row in result, but got ${results.size} rows. SQL: "$sql""""
        )
      )
  }
/*
def pgSelectOne[R, A](
    ctx: TransactionContextPg,
    sql: String,
    mapResult: java.sql.ResultSet => A
): ZIO[R, InfraFailure, Option[A]] =
  pgSelectOne(ctx, sql, _ => (), mapResult)
 */
