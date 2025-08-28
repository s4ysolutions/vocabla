package solutions.s4y.infra.pgsql.wrappers

import solutions.s4y.infra.pgsql.tx.TransactionContextPg
import solutions.s4y.vocabla.app.repo.tx.TransactionContext
import solutions.s4y.vocabla.domain.identity.Identifier
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import zio.ZIO

import java.sql.{PreparedStatement, Statement}

/** Function that executes an insert statement and returns the generated ID
  * Usage: pgInsertWithId("INSERT INTO table (col1, col2) VALUES (?, ?)",
  * _.setString(1, "value").setLong(2, 123L))
  */
def pgInsertOne[T](
    sql: String,
    setParams: PreparedStatement => Unit
): ZIO[TransactionContextPg, String, Boolean] =
  pgWithConnection { connection =>
    ZIO.scoped {
      for {
        st <- ZIO
          .fromAutoCloseable(
            ZIO.attempt(
              connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            )
          )
          .mapError(error =>
            s"Failed to prepare statement \"$sql\": ${error.getMessage}"
          )
        _ <- ZIO
          .attempt(setParams(st))
          .mapError(error => s"Failed to set parameters: ${error.getMessage}")
        rowsAffected <- ZIO
          .attempt(st.executeUpdate())
          .mapError(error =>
            s"Failed to execute statement: ${error.getMessage}"
          )
      } yield rowsAffected > 0
    }
  }
