package solutions.s4y.infra.pgsql.wrappers

import solutions.s4y.vocabla.app.repo.tx.TransactionContext
import solutions.s4y.zio.e
import zio.ZIO

import scala.util.Using

def selectOne[A](
    sql: String,
    setParams: java.sql.PreparedStatement => Unit,
    mapResult: java.sql.ResultSet => A
): ZIO[TransactionContext, String, Option[A]] = withConnection { connection =>
  ZIO.scoped {
    ZIO
      .fromAutoCloseable(
        ZIO
          .attempt(connection.prepareStatement(sql))
          .e(th => s"Failed to prepare statement \"$sql\": ${th.getMessage}")
      )
      .flatMap { st =>
        ZIO
          .attempt {
            setParams(st)
            Using.resource(st.executeQuery()) { rs =>
              if (rs.next()) {
                Some(mapResult(rs))
              } else {
                None
              }
            }
          }
          .e(th => s"Failed to execute query: ${th.getMessage}")
      }
  }
}
