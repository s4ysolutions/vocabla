package solutions.s4y.infra.mvstore.tx

import solutions.s4y.vocabla.app.repo.tx.{Transaction, TransactionManager}
import zio.{IO, ZIO}

final class TransactionManagerMVStore extends TransactionManager:
  override def withTransactionZIO[A](
      f: Transaction => IO[String, A]
  ): IO[String, A] =
    f(null)
