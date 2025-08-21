package solutions.s4y.infra.pgsql.macros

import solutions.s4y.infra.pgsql.tx.TransactionContextPg

import scala.quoted.*
import solutions.s4y.vocabla.app.repo.tx.TransactionContext
import solutions.s4y.vocabla.domain.identity.Identifier
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import zio.ZIO

import java.sql.{PreparedStatement, Statement}

/** Macro that accepts SQL string for prepared statement and expression to set
  * params, returns generated ID Usage: executeInsertWithId("INSERT INTO table
  * (col1, col2) VALUES (?, ?)", _.setString(1, "value").setLong(2, 123L))
  */
inline def executeInsertWithId[T](
    inline sql: String,
    inline setParams: PreparedStatement => Unit
): ZIO[TransactionContext, String, Identifier[T]] =
  ${ executeInsertWithIdImpl[T]('sql, 'setParams) }

private def executeInsertWithIdImpl[T: Type](
    sql: Expr[String],
    setParams: Expr[PreparedStatement => Unit]
)(using Quotes): Expr[ZIO[TransactionContext, String, Identifier[T]]] = {
  '{
    withConnection { connection =>
      ZIO.scoped {
        for {
          st <- ZIO
            .fromAutoCloseable(
              ZIO.attempt(
                connection
                  .prepareStatement($sql, Statement.RETURN_GENERATED_KEYS)
              )
            )
            .mapError(error =>
              s"Failed to prepare statement: ${error.getMessage}"
            )
          _ <- ZIO
            .attempt($setParams(st))
            .mapError(error => s"Failed to set parameters: ${error.getMessage}")
          rowsAffected <- ZIO
            .attempt(st.executeUpdate())
            .mapError(error =>
              s"Failed to execute statement: ${error.getMessage}"
            )
          _ <- ZIO.when(rowsAffected == 0)(
            ZIO.fail("No rows were inserted")
          )
          rs <- ZIO
            .fromAutoCloseable(ZIO.attempt(st.getGeneratedKeys))
            .mapError(error =>
              s"Failed to get generated keys: ${error.getMessage}"
            )
          id <- ZIO
            .attempt {
              if (rs.next()) {
                rs.getLong(1).identifier[T]
              } else {
                throw new Exception("No generated key returned")
              }
            }
            .mapError(error =>
              s"Failed to extract generated key: ${error.getMessage}"
            )
        } yield id
      }
    }
  }
}
