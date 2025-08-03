package solutions.s4y.vocabla.me.app

import solutions.s4y.vocabla.me.infra.dummy.DummyMeRepository
import zio.{Layer, ZLayer}

case object DummyMeService:
  def makeLayer: Layer[Nothing, MeService] =
    ZLayer.succeed(MeService(DummyMeRepository.live))
