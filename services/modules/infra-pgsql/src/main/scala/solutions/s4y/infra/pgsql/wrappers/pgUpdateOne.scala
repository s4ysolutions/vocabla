package solutions.s4y.infra.pgsql.wrappers

import solutions.s4y.i18n.ResourcesStringsResolver.default
import solutions.s4y.i18n.t
import solutions.s4y.infra.pgsql.tx.{TransactionContextPg, TransactionManagerPg}
import solutions.s4y.vocabla.app.repo.error.InfraFailure
import zio.ZIO

import java.sql.PreparedStatement

def pgUpdateOne[R](
    sql: String,
    setParams: PreparedStatement => Unit
)(using TransactionContextPg): ZIO[R, InfraFailure, Boolean] =
  pgUpdate(sql, setParams)
    .flatMap(count =>
      if count == 1 then ZIO.succeed(true)
      else if count == 0 then ZIO.succeed(false)
      else
        ZIO.fail(
          InfraFailure(
            t"""Expected exactly one row to be affected, but got $count rows. SQL: "$sql""""
          )
        )
    )
