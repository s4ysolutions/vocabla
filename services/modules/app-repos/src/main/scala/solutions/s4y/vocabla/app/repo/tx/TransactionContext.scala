package solutions.s4y.vocabla.app.repo.tx

import solutions.s4y.vocabla.app.repo.error.InfraFailure
import zio.IO

trait TransactionContext:
  def rollback(): IO[InfraFailure, Unit]
