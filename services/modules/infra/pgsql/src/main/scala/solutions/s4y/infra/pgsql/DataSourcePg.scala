package solutions.s4y.infra.pgsql

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import solutions.s4y.zio.e
import zio.{IO, ZIO, ZLayer}

import java.sql.Connection

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
  val live: ZLayer[PgSqlConfig, String, DataSourcePg] =
    ZLayer.scoped {
      for {
        config <- ZIO.service[PgSqlConfig]
        hikariConfig = {
          val hc = new HikariConfig()
          hc.setJdbcUrl(config.url)
          hc.setUsername(config.user)
          hc.setPassword(config.password)
          hc
        }
        ds <- ZIO.acquireRelease(
          ZIO
            .attemptBlocking(
              new DataSourcePg(new HikariDataSource(hikariConfig))
            )
            .e(th => th.getMessage)
        )(ds => ZIO.attemptBlocking(ds.close()).orDie)
      } yield ds
    }

}
