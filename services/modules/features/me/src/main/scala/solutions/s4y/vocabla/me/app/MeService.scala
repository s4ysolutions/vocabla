package solutions.s4y.vocabla.me.app

import solutions.s4y.vocabla.domain.model.Identifier
import solutions.s4y.vocabla.me.app.ports.GetMeUseCase
import solutions.s4y.vocabla.me.app.repo.MeRepository
import solutions.s4y.vocabla.me.domain.model.Me
import zio.IO

class MeService(private val meRepository: MeRepository) extends GetMeUseCase:
  override def getMe: IO[String, Identifier[Me]] = meRepository.me
