package solutions.s4y.infra.pgsql.wrappers

import solutions.s4y.vocabla.app.repo.tx.TransactionContext
import solutions.s4y.zio.e
import zio.{Chunk, ZIO}

import scala.util.Using

def pgSelectMany[A](
    sql: String,
    setParams: java.sql.PreparedStatement => Unit,
    mapResult: java.sql.ResultSet => A
): ZIO[TransactionContext, String, Chunk[A]] = pgWithConnection { connection =>
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
              val buffer = scala.collection.mutable.ArrayBuffer.empty[A]
              while (rs.next()) {
                buffer += mapResult(rs)
              }
              Chunk.fromIterable(buffer)
            }
          }
          .e(th => s"Failed to execute query: ${th.getMessage}")
      }
  }
}
