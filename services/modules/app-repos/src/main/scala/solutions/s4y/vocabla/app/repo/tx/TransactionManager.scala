package solutions.s4y.vocabla.app.repo.tx

import zio.{IO, ZIO}

trait TransactionManager[TR <: Transaction, TX <: TransactionContext]:
  def transaction[R, A](
      unitOfWork: ZIO[R & TR & TX, String, A]
  ): ZIO[R, String, A]
