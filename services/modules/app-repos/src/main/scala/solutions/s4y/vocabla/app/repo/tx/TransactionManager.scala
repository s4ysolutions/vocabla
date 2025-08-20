package solutions.s4y.vocabla.app.repo.tx

import zio.{IO, ZIO}

trait TransactionManager:
  def transaction[R, A](
      zio: ZIO[R & TransactionContext, String, A]
  ): ZIO[R, String, A]
