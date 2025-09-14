package solutions.s4y.vocabla.infra.pgsql

import io.github.cdimascio.dotenv.DotenvBuilder
import solutions.s4y.i18n.ResourcesStringsResolver.default
import solutions.s4y.i18n.t
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
    /*(layerWithDataSourcePg >>> ZLayer.fromZIO(
      cleardb
    )) >>> (*/ layerWithDataSourcePg >>> TransactionManagerPg.layer ++ TagRepositoryPg.layer ++ EntryRepositoryPg.layer ++ TagAssociationRepositoryPg.layer

  val testSystem: ZIO[Any, Throwable, Unit] = ZIO
    .attempt(DotenvBuilder().filename(".env_test").load())
    .flatMap(dotenv =>
      TestSystem.putEnv("PGSQL_PASSWORD", dotenv.get("PGSQL_PASSWORD")) *>
        TestSystem.putEnv("PGSQL_SCHEMA", "vocabla_test") *>
        TestSystem.putEnv("PGSQL_HOST", dotenv.get("PGSQL_HOST"))
    )
    .ignore

  val cleardb: ZIO[DataSourcePg, InfraFailure, Unit] =
    ZIO.serviceWithZIO[DataSourcePg] { ds =>
      ZIO.scoped {
        ZIO
          .fromAutoCloseable(ds.getConnection)
          .flatMap { connection =>
            ZIO
              .fromAutoCloseable(ZIO.attempt(connection.createStatement()))
              .flatMap { stmt =>
                ZIO.attemptBlocking {
                  stmt.execute(
                    "DROP TABLE IF EXISTS tag_entry_associations CASCADE"
                  )
                  stmt.execute("DROP TABLE IF EXISTS tags CASCADE")
                  stmt.execute("DROP TABLE IF EXISTS entries CASCADE")
                  stmt.execute("DROP TABLE IF EXISTS users CASCADE")
                  stmt.execute("DROP TYPE IF EXISTS user_admin")
                  stmt.execute("DROP TYPE IF EXISTS user_student")
                }.unit
              }
          }
          .mapError {
            case infra: InfraFailure => infra
            case th: Throwable =>
              InfraFailure(t"Failed to clear database", Some(th))
          }
      }
    }

end Fixture
