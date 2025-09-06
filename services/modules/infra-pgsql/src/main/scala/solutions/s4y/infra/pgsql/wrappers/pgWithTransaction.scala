package solutions.s4y.infra.pgsql.wrappers

import solutions.s4y.infra.pgsql.tx.{TransactionContextPg, TransactionManagerPg}
import solutions.s4y.vocabla.app.repo.error.InfraFailure
import zio.ZIO

def pgWithTransaction[R, A](
    log: String,
    effect: TransactionContextPg ?=> ZIO[R, InfraFailure, A]
): ZIO[TransactionManagerPg & R, InfraFailure, A] = {
  ZIO.serviceWithZIO[TransactionManagerPg](_.transaction(log, effect))
}
