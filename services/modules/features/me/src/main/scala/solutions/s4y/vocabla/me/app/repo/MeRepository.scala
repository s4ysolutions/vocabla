package solutions.s4y.vocabla.me.app.repo

import solutions.s4y.vocabla.domain.model.Identifier
import solutions.s4y.vocabla.me.domain.model.Me
import zio.IO

trait MeRepository:
  def me: IO[String, Identifier[Me]]
