package solutions.s4y.vocabla.app.repo.tx

import zio.IO

trait TransactionContext:
  def rollback(): IO[String, Unit]
