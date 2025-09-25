package solutions.s4y.vocabla.infra.pgsql

import solutions.s4y.infra.pgsql.tx.{TransactionContextPg, TransactionManagerPg}
import solutions.s4y.infra.pgsql.{DataSourcePg, PgSqlConfig}
import solutions.s4y.vocabla.app.repo.error.InfraFailure
import zio.ZLayer

object InfraPgLive:
  private val repositoriesLayer: ZLayer[
    DataSourcePg,
    InfraFailure,
    TransactionManagerPg & UserRepositoryPg & EntryRepositoryPg &
      TagRepositoryPg & TagAssociationRepositoryPg & KnownLanguagesRepositoryPg & LearnLanguagesRepositoryPg
  ] = for {
    tm <- TransactionManagerPg.layer
    user <- UserRepositoryPg.layer
    entry <- EntryRepositoryPg.layer
    tag <- TagRepositoryPg.layer
    tagAssoc <- TagAssociationRepositoryPg.layer
    knownLang <- KnownLanguagesRepositoryPg.layer
    learnLang <- LearnLanguagesRepositoryPg.layer
  } yield tm ++ user ++ entry ++ tag ++ tagAssoc ++ knownLang ++ learnLang

  type TX = TransactionContextPg

  val layer: ZLayer[
    Any,
    InfraFailure,
    TransactionManagerPg & UserRepositoryPg & EntryRepositoryPg &
      TagRepositoryPg & TagAssociationRepositoryPg & KnownLanguagesRepositoryPg & LearnLanguagesRepositoryPg
  ] =
    PgSqlConfig.layer >>> DataSourcePg.layer >>> repositoriesLayer
