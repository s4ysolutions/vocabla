package solutions.s4y.vocabla.infra.pgsql

import io.github.cdimascio.dotenv.DotenvBuilder
import solutions.s4y.infra.pgsql.tx.TransactionManagerPg
import solutions.s4y.infra.pgsql.{DataSourcePg, PgSqlConfig}
import zio.test.TestSystem
import zio.{ZIO, ZLayer}
object Fixture:
  val layerWithDataSourcePg: ZLayer[Any, String, DataSourcePg] =
    PgSqlConfig.layer >>> DataSourcePg.layer

  val layerWithTransactionManager: ZLayer[Any, String, TransactionManagerPg] =
    layerWithDataSourcePg >>> TransactionManagerPg.layer

  val layerWithEntryRepository: ZLayer[
    Any,
    String,
    TransactionManagerPg & TagRepositoryPg & EntryRepositoryPg
  ] = {
    ZLayer(
      ZIO.logDebug("Init")
    ) >>> layerWithDataSourcePg >>> (TransactionManagerPg.layer ++ TagRepositoryPg.layer ++ EntryRepositoryPg.layer)
  }

  val layerWithTagAssociationRepository: ZLayer[
    Any,
    String,
    TransactionManagerPg & TagRepositoryPg & EntryRepositoryPg &
      TagAssociationRepositoryPg
  ] = {
    ZLayer(
      ZIO.logDebug("Init")
    ) >>> layerWithDataSourcePg >>> (TransactionManagerPg.layer ++ TagRepositoryPg.layer ++ EntryRepositoryPg.layer ++ TagAssociationRepositoryPg.layer)
  }

  val layer: ZLayer[Any, String, TransactionManagerPg & TagRepositoryPg] = {
    ZLayer(
      ZIO.logDebug("Init")
    ) >>> layerWithDataSourcePg >>> (TransactionManagerPg.layer ++ TagRepositoryPg.layer)
  }

  val testSystem: ZIO[Any, Throwable, Unit] = ZIO
    .attempt(DotenvBuilder().filename(".env_test").load())
    .flatMap(dotenv =>
      TestSystem.putEnv("PGSQL_PASSWORD", dotenv.get("PGSQL_PASSWORD")) *>
        TestSystem.putEnv("PGSQL_SCHEMA", "vocabla_test")
    )
end Fixture
