package solutions.s4y.infra.pgsql

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import solutions.s4y.i18n.ResourcesStringsResolver.default
import solutions.s4y.i18n.t
import solutions.s4y.vocabla.app.repo.error.InfraFailure
import solutions.s4y.vocabla.app.repo.error.InfraFailure.mapThrowable
import zio.{IO, ZIO, ZLayer}

import java.sql.Connection
import scala.util.Using

class DataSourcePg(val dataSource: HikariDataSource):

  def getConnection: IO[InfraFailure, Connection] = ZIO
    .attemptBlocking(
      dataSource.getConnection)
    .mapThrowable(t"Failed to get connection from DataSource")

  def close(): IO[InfraFailure, Unit] = ZIO
    .attemptBlocking(dataSource.close())
    .mapThrowable(t"Failed to close DataSource")

object DataSourcePg {
  val layer: ZLayer[PgSqlConfig, InfraFailure, DataSourcePg] =
    ZLayer.scoped {
      for {
        _ <- ZIO.logDebug("Initializing DataSourcePg")
        config <- ZIO.service[PgSqlConfig]
        hikariConfig = {
          val hc = new HikariConfig()
          hc.setDriverClassName("org.postgresql.Driver")
          // hc.setLeakDetectionThreshold(2000) // TODO: test only
          hc.setMaximumPoolSize(1) // TODO: adjust pool size as needed
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
        )(ds =>
          ZIO.logDebug("Closing DataSourcePg") *>
            ZIO.attemptBlocking(ds.close()).orDie
            <* ZIO.logDebug("DataSourcePg closed")
        )
      } yield ds
    }

}
