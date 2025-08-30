package solutions.s4y.infra.pgsql.wrappers

import solutions.s4y.infra.pgsql.tx.{TransactionContextPg, TransactionManagerPg}
import solutions.s4y.vocabla.app.repo.error.InfraFailure
import zio.ZIO

import java.sql.PreparedStatement

def pgDeleteOne[R](
    sql: String,
    setParams: PreparedStatement => Unit
)(using TransactionContextPg): ZIO[R, InfraFailure, Boolean] =
  pgUpdateOne[R](sql, setParams)
