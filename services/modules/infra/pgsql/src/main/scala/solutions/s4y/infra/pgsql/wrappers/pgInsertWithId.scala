package solutions.s4y.infra.pgsql.wrappers
import solutions.s4y.i18n.ResourcesStringsResolver.default
import solutions.s4y.i18n.t
import solutions.s4y.infra.pgsql.tx.TransactionContextPg
import solutions.s4y.vocabla.app.repo.error.InfraFailure
import solutions.s4y.vocabla.app.repo.error.InfraFailure.mapThrowable
import solutions.s4y.vocabla.domain.identity.Identifier
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import zio.ZIO

import java.sql.{PreparedStatement, Statement}

def pgInsertWithId[R, A](
    sql: String,
    setParams: PreparedStatement => Unit
)(using ctx: TransactionContextPg): ZIO[R, InfraFailure, Identifier[A]] =
  ZIO.scoped {
    for {
      st <- ZIO
        .fromAutoCloseable(
          ZIO.attempt(
            ctx.connection
              .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
          )
        )
        .mapThrowable(t"""Failed to prepare statement: "$sql"""")
      _ <- ZIO
        .attempt(setParams(st))
        .mapThrowable(t"""Failed to set parameters for: "$sql"""")
      rowsAffected <- ZIO
        .attempt(st.executeUpdate())
        .mapThrowable(t"""Failed to execute statement: "$sql"""")
      _ <- ZIO.when(rowsAffected == 0)(
        ZIO.fail(InfraFailure(t"""No rows were affected: "$sql""""))
      )
      rs <- ZIO
        .fromAutoCloseable(ZIO.attempt(st.getGeneratedKeys))
        .mapThrowable(t"""Failed to get generated keys for: "$sql"""")
      id <- ZIO
        .attempt {
          if (rs.next()) {
            rs.getLong(1).identifier[A]
          } else {
            throw new Exception("No generated key returned")
          }
        }
        .mapThrowable(t"""Failed to extract generated key for: "$sql"""")
    } yield id
  }
