package solutions.s4y.infra.pgsql.wrappers

import solutions.s4y.infra.pgsql.tx.TransactionContextPg
import solutions.s4y.vocabla.domain.identity.Identifier
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import zio.ZIO

import java.sql.{PreparedStatement, Statement}

/** Function that executes an insert statement and returns the generated ID
  * Usage: pgInsertWithId("INSERT INTO table (col1, col2) VALUES (?, ?)",
  * _.setString(1, "value").setLong(2, 123L))
  */
def pgInsertWithId[T](
    sql: String,
    setParams: PreparedStatement => Unit
): ZIO[TransactionContextPg, String, Identifier[T]] =
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
        _ <- ZIO.when(rowsAffected == 0)(
          ZIO.fail("No rows were affected")
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
