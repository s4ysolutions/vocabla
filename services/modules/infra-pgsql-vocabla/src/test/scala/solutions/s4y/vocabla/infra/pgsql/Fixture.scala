package solutions.s4y.vocabla.infra.pgsql

import io.github.cdimascio.dotenv.DotenvBuilder
import solutions.s4y.infra.pgsql.tx.TransactionManagerPg
import solutions.s4y.infra.pgsql.{DataSourcePg, PgSqlConfig}
import solutions.s4y.vocabla.app.repo.error.InfraFailure
import zio.test.TestSystem
import zio.{ZIO, ZLayer}
object Fixture:
  val layerWithDataSourcePg: ZLayer[Any, InfraFailure, DataSourcePg] =
    PgSqlConfig.layer >>> DataSourcePg.layer

  val layerWithTransactionManager
      : ZLayer[Any, InfraFailure, TransactionManagerPg] =
    layerWithDataSourcePg >>> TransactionManagerPg.layer

  val layerWithEntryRepository: ZLayer[
    Any,
    InfraFailure,
    TransactionManagerPg & EntryRepositoryPg
  ] =
    ZLayer.succeed(
      ZIO.logDebug("create layerWithEntryRepository")
    ) >>> layerWithDataSourcePg >>> (TransactionManagerPg.layer ++ EntryRepositoryPg.layer)

  val layerWithTagRepository
      : ZLayer[Any, InfraFailure, TransactionManagerPg & TagRepositoryPg] =
    layerWithDataSourcePg >>> (TransactionManagerPg.layer ++ TagRepositoryPg.layer)

  val layerWithTagAssociationRepository: ZLayer[
    Any,
    InfraFailure,
    TransactionManagerPg & TagRepositoryPg & EntryRepositoryPg &
      TagAssociationRepositoryPg
  ] =
    layerWithDataSourcePg >>> (TransactionManagerPg.layer ++ TagRepositoryPg.layer ++ EntryRepositoryPg.layer ++ TagAssociationRepositoryPg.layer)

  val testSystem: ZIO[Any, Throwable, Unit] = ZIO
    .attempt(DotenvBuilder().filename(".env_test").load())
    .flatMap(dotenv =>
      TestSystem.putEnv("PGSQL_PASSWORD", dotenv.get("PGSQL_PASSWORD")) *>
        TestSystem.putEnv("PGSQL_SCHEMA", "vocabla_test")
    )
    .ignore
end Fixture
