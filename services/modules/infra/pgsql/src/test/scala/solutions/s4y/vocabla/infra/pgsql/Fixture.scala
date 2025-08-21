package solutions.s4y.vocabla.infra.pgsql

import io.github.cdimascio.dotenv.DotenvBuilder
import solutions.s4y.infra.pgsql.tx.TransactionManagerPg
import solutions.s4y.infra.pgsql.{DataSourcePg, PgSqlConfig}
import solutions.s4y.vocabla.app.repo.TagRepository
import solutions.s4y.vocabla.app.repo.tx.TransactionManager
import zio.test.TestSystem
import zio.{ZIO, ZLayer}

object Fixture {
  val dataSourcePgLayer: ZLayer[Any, String, DataSourcePg] =
    PgSqlConfig.layer >>> DataSourcePg.layer

  val transactionManagerLayer: ZLayer[Any, String, TransactionManagerPg] =
    dataSourcePgLayer >>> TransactionManagerPg.layer

  val layer: ZLayer[Any, String, TransactionManager & TagRepository] = {
    ZLayer(
      ZIO.logDebug("Init")
    ) >>> dataSourcePgLayer >>> (TransactionManagerPg.layer ++ TagRepositoryPg.layer)
  }

  val testSystem: ZIO[Any, Throwable, Unit] = ZIO
    .attempt(DotenvBuilder().filename(".env_test").load())
    .flatMap(dotenv =>
      TestSystem.putEnv("PGSQL_PASSWORD", dotenv.get("PGSQL_PASSWORD")) *>
        TestSystem.putEnv("PGSQL_SCHEMA", "vocabla_test")
    )
}
