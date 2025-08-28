package solutions.s4y.infra.pgsql.tx

import solutions.s4y.infra.pgsql.{DataSourcePg, PgSqlConfig}
import solutions.s4y.vocabla.app.repo.tx.TransactionManager
import solutions.s4y.zio.e
import zio.{IO, ZIO, ZLayer}

case class TransactionManagerPg(private val ds: DataSourcePg)
    extends TransactionManager[TransactionPg, TransactionContextPg]:

  override def transaction[R, A](
      unitOfWork: ZIO[R & TransactionPg & TransactionContextPg, String, A]
  ): ZIO[R, String, A] =
    ZIO
      .scoped {
        ZIO
          .acquireRelease(
            ds.getConnection.flatMap(connection =>
              ZIO
                .attempt {
                  connection.setAutoCommit(false)
                  (TransactionPg(connection), TransactionContextPg(connection))
                }
                .e(th => th.getMessage)
            )
          )(tx => ZIO.attempt(tx._2.connection.close()).orDie)
          .flatMap(tx =>
            unitOfWork
              .provideSomeLayer(ZLayer.succeed(tx._1) ++ ZLayer.succeed(tx._2))
              .tapBoth(
                err => ZIO.attempt(tx._1.rollback()).orDie,
                _ => ZIO.attempt(tx._2.connection.commit()).orDie
              )
          )
      }

object TransactionManagerPg:
  val layer: ZLayer[DataSourcePg, String, TransactionManagerPg] =
    ZLayer.fromZIO(
      ZIO.config(PgSqlConfig.pgSqlConfig).orDie
    ) >>> DataSourcePg.layer >>>
      ZLayer.fromFunction(TransactionManagerPg(_))
end TransactionManagerPg
