package solutions.s4y.vocabla.infra.pgsql

import solutions.s4y.infra.pgsql.tx.TransactionManagerPg
import solutions.s4y.infra.pgsql.{DataSourcePg, PgSqlConfig}
import solutions.s4y.vocabla.app.repo.error.InfraFailure
import zio.ZLayer

object InfraPgLive:
  private val repositoriesLayer: ZLayer[
    DataSourcePg,
    InfraFailure,
    TransactionManagerPg & UserRepositoryPg & EntryRepositoryPg &
      TagRepositoryPg & TagAssociationRepositoryPg
  ] = for {
    tm <- TransactionManagerPg.layer
    user <- UserRepositoryPg.layer
    entry <- EntryRepositoryPg.layer
    tag <- TagRepositoryPg.layer
    tagAssoc <- TagAssociationRepositoryPg.layer
  } yield tm ++ user ++ entry ++ tag ++ tagAssoc

  val layer: ZLayer[
    Any,
    InfraFailure,
    TransactionManagerPg & UserRepositoryPg & EntryRepositoryPg &
      TagRepositoryPg & TagAssociationRepositoryPg
  ] =
    PgSqlConfig.layer >>> DataSourcePg.layer >>> repositoriesLayer
