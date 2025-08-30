package solutions.s4y.vocabla.app.repo.tx

import solutions.s4y.vocabla.app.repo.error.InfraFailure
import zio.ZIO

trait TransactionManager[TX <: TransactionContext: zio.Tag]:
  def transaction[R, A](
      effect: TX ?=> ZIO[R, InfraFailure, A]
  ): ZIO[R, InfraFailure, A]

/*
  def transaction[R, A](
                         effect: TX => ZIO[R, InfraFailure, A]
                       ): ZIO[R, InfraFailure, A]
  def transaction[R, A](
      zio: ZIO[TX & R, InfraFailure, A]
  ): ZIO[TX & R, InfraFailure, A] =
    transaction[R, A](ctx => effect.provideSomeEnvironment(env => env.add(ctx))
 */
