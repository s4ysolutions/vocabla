package solutions.s4y.vocabla.app.repo.tx

import zio.IO

trait TransactionManager:
  def withTransactionZIO[A](f: Transaction => IO[String, A]): IO[String, A]
