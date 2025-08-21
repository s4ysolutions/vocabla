package solutions.s4y.infra.pgsql

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import solutions.s4y.zio.e
import zio.{IO, ZIO, ZLayer}

import java.sql.Connection
import scala.util.Using

class DataSourcePg(val dataSource: HikariDataSource) {

  def getConnection: IO[String, Connection] = {
    ZIO
      .attemptBlocking(dataSource.getConnection)
      .refineToOrDie[Throwable]
      .e(th => th.getMessage)
  }

  def close(): IO[String, Unit] = {
    ZIO
      .attemptBlocking(dataSource.close())
      .refineToOrDie[Throwable]
      .e(th => th.getMessage)
  }
}

object DataSourcePg {
  val layer: ZLayer[PgSqlConfig, String, DataSourcePg] =
    ZLayer.scoped {
      for {
        _ <- ZIO.logDebug("Initializing DataSourcePg")
        config <- ZIO.service[PgSqlConfig]
        hikariConfig = {
          val hc = new HikariConfig()
          hc.setJdbcUrl(config.url)
          hc.setUsername(config.user)
          hc.setPassword(config.password)
          hc.setSchema(config.schema)
          hc
        }
        ds <- ZIO.acquireRelease(
          ZIO.attempt {
            val ds = new DataSourcePg(new HikariDataSource(hikariConfig))
            val schema = ds.dataSource.getSchema
            Using.Manager { use =>
              val connection = use(ds.dataSource.getConnection)
              val statement = use(connection.createStatement())
              statement.execute(s"CREATE SCHEMA IF NOT EXISTS $schema")
            }
            ds
          }.orDie <* ZIO.logDebug("DataSourcePg initialized")
        )(ds => ZIO.attemptBlocking(ds.close()).orDie)
      } yield ds
    }

}
