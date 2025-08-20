package solutions.s4y.infra.pgsql.tx

import solutions.s4y.infra.pgsql.DataSourcePg
import solutions.s4y.vocabla.app.repo.tx.{TransactionContext, TransactionManager}
import solutions.s4y.zio.e
import zio.{ZIO, ZLayer}

case class TransactionManagerPg(private val ds: DataSourcePg)
    extends TransactionManager:
  override def transaction[R, A](
      unitOfWork: ZIO[R & TransactionContext, String, A]
  ): ZIO[R, String, A] =
    ZIO
      .scoped {
        ZIO
          .acquireRelease(
            ds.getConnection.flatMap(connection =>
              ZIO
                .attempt {
                  connection.setAutoCommit(false)
                  TransactionContextPg(connection)
                }
                .e(th => th.getMessage)
            )
          )(t => ZIO.attempt(t.connection.close()).orDie)
          .flatMap(tx =>
            unitOfWork
              .provideSomeLayer(ZLayer.succeed(tx))
              .tapBoth(
                err => ZIO.attempt(tx.connection.rollback()).orDie,
                _ => ZIO.attempt(tx.connection.commit()).orDie
              )
          )
      }
