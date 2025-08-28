package solutions.s4y.vocabla.app.repo.tx

import zio.IO

trait Transaction:
  def rollback(): IO[String, Unit]
