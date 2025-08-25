package solutions.s4y.vocabla.infra.pgsql

import solutions.s4y.infra.pgsql.tx.TransactionManagerPg
import solutions.s4y.infra.pgsql.{DataSourcePg, PgSqlConfig}
import solutions.s4y.vocabla.app.repo.tx.TransactionManager
import solutions.s4y.vocabla.app.repo.{
  EntryRepository,
  TagAssociationRepository,
  TagRepository,
  UserRepository
}
import solutions.s4y.vocabla.domain.Entry
import zio.ZLayer

object InfraPgLive:
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
    TransactionManager & UserRepository & EntryRepository & TagRepository &
      TagAssociationRepository[Entry]
  ] =
    PgSqlConfig.layer >>> DataSourcePg.layer >>> repositoriesLayer
