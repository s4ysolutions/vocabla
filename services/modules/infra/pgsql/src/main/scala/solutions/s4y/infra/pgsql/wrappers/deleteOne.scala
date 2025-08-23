package solutions.s4y.infra.pgsql.wrappers

import solutions.s4y.vocabla.app.repo.tx.TransactionContext
import zio.ZIO

import java.sql.PreparedStatement

/** Executes an update statement that affects one row in the database. This
  * function is typically used for operations like updating a single record.
  * Usage: deleteOne("UPDATE table SET col1 = ? WHERE id = ?", _.setString(1,
  * "newValue").setLong(2, 123L))
  * @param sql
  *   SQL statement to execute, typically an UPDATE
  * @param setParams
  *   Function to set parameters on the PreparedStatement.
  * @return
  *   A ZIO effect that completes successfully if the update affects exactly one
  *   row,
  */
def deleteOne(
    sql: String,
    setParams: PreparedStatement => Unit
): ZIO[TransactionContext, String, Boolean] =
  withConnection { connection =>
    ZIO.scoped {
      for {
        st <- ZIO
          .fromAutoCloseable(
            ZIO.attempt(
              connection.prepareStatement(sql)
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
