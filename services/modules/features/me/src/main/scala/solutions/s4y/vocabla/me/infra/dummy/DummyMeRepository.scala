package solutions.s4y.vocabla.me.infra.dummy

import solutions.s4y.vocabla.domain.model.Identifier
import solutions.s4y.vocabla.me.app.repo.MeRepository
import solutions.s4y.vocabla.me.domain.model.Me
import zio.{IO, ZIO}

import java.util.UUID

object DummyMeRepository:
  private val me = Identifier[Me, UUID](
    UUID.fromString("00000000-0000-0000-0000-000000000010")
  )

  val live: MeRepository = new MeRepository {
    override def me: IO[String, Identifier[Me]] =
      ZIO.succeed(DummyMeRepository.me)
  }
