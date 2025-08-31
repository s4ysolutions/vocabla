package solutions.s4y.infra.pgsql.tx

import solutions.s4y.i18n.ResourcesStringsResolver.default
import solutions.s4y.i18n.t
import solutions.s4y.infra.pgsql.{DataSourcePg, PgSqlConfig}
import solutions.s4y.vocabla.app.repo.error.InfraFailure
import solutions.s4y.vocabla.app.repo.error.InfraFailure.mapThrowable
import solutions.s4y.vocabla.app.repo.tx.TransactionManager
import zio.{Exit, ZIO, ZLayer}

case class TransactionManagerPg(private val ds: DataSourcePg)
    extends TransactionManager[TransactionContextPg]:

  override def transaction[R, A](
      effect: TransactionContextPg ?=> ZIO[R, InfraFailure, A]
  ): ZIO[R, InfraFailure, A] =
    transactionE[R, A](ctx => effect(using ctx))

  private def transactionE[R, A](
      effect: TransactionContextPg => ZIO[R, InfraFailure, A]
  ): ZIO[R, InfraFailure, A] =
    ZIO
      .scoped(
        ZIO
          .acquireReleaseExitWith(
            ds.getConnection.flatMap(connection =>
              ZIO
                .attempt {
                  connection.setAutoCommit(false)
                  TransactionContextPg(connection)
                }
                .mapThrowable(t"Failed to start transaction")
            ) <* ZIO.logTrace("Transaction started")
          )((tx, exit) =>
            (exit match {
              case Exit.Success(_) =>
                ZIO
                  .attempt(tx.connection.commit())
                  .mapThrowable(t"Failed to commit transaction")
              case Exit.Failure(cause) =>
                ZIO
                  .attempt(tx.connection.rollback())
                  .mapThrowable(t"Failed to rollback transaction")
            }).ignore
              .as(tx.connection.close())
              .mapThrowable(t"Failed to close transaction")
              .ignore *>
              ZIO.logTrace("Transaction closed")
          )(effect)
      )

object TransactionManagerPg:
  val layer: ZLayer[DataSourcePg, InfraFailure, TransactionManagerPg] =
    ZLayer.fromZIO(
      ZIO.config(PgSqlConfig.pgSqlConfig).orDie
    ) >>> DataSourcePg.layer >>>
      ZLayer.fromFunction(TransactionManagerPg(_))

  private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)
end TransactionManagerPg
