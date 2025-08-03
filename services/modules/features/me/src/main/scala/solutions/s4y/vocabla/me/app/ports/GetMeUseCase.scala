package solutions.s4y.vocabla.me.app.ports

import solutions.s4y.vocabla.domain.model.Identifier
import solutions.s4y.vocabla.me.domain.model.Me
import zio.IO

trait GetMeUseCase:
  def getMe: IO[String, Identifier[Me]]
