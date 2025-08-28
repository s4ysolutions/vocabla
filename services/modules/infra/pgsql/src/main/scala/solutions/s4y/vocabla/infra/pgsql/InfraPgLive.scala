package solutions.s4y.vocabla.infra.pgsql

import solutions.s4y.infra.pgsql.tx.{
  TransactionContextPg,
  TransactionManagerPg,
  TransactionPg
}
import solutions.s4y.infra.pgsql.{DataSourcePg, PgSqlConfig}
import zio.ZLayer

object InfraPgLive:
  type TR = TransactionPg
  type TX = TransactionContextPg
  // enforce sequential initialization of layers
  private val repositoriesLayer = for {
    tm <- TransactionManagerPg.layer
    user <- UserRepositoryPg.layer
    entry <- EntryRepositoryPg.layer
    tag <- TagRepositoryPg.layer
    tagAssoc <- TagAssociationRepositoryPg.layer
  } yield tm ++ user ++ entry ++ tag ++ tagAssoc

  val layer: ZLayer[
    Any,
    String,
    TransactionManagerPg & UserRepositoryPg & EntryRepositoryPg &
      TagRepositoryPg & TagAssociationRepositoryPg
  ] =
    PgSqlConfig.layer >>> DataSourcePg.layer >>> repositoriesLayer
