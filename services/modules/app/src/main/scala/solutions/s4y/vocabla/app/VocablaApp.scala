package solutions.s4y.vocabla.app

import org.slf4j.LoggerFactory
import solutions.s4y.vocabla.app.ports.{PingCommand, PingUseCase}
import solutions.s4y.vocabla.app.repo.EntryRepository
import solutions.s4y.vocabla.app.repo.tx.TransactionManager
import solutions.s4y.vocabla.infra.pgsql.InfraPgLive
import zio.{ZIO, ZLayer, durationInt}

final class VocablaApp(
    private val tm: TransactionManager,
    private val entriesRepository: EntryRepository
) extends PingUseCase: // , CreateEntryUseCase:
  VocablaApp.logger.debug("Creating VocablaApp instance")

  override def apply[R](
      pingCommand: PingCommand
  ): ZIO[Any, String, PingCommand.Response] =
    ZIO
      .succeed("PONG from VocablaApp: " + pingCommand.payload)
      .delay(200.millis)

end VocablaApp

object VocablaApp:
  val layer: ZLayer[Any, String, PingUseCase] =
    InfraPgLive.layer >>> ZLayer.fromFunction(
      new VocablaApp(_, _)
    )

  private val logger = LoggerFactory.getLogger(VocablaApp.getClass)
end VocablaApp
