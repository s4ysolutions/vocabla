package solutions.s4y.vocabla.app

import org.slf4j.LoggerFactory
import solutions.s4y.vocabla.app.ports.{
  CreateEntryCommand,
  CreateEntryUseCase,
  CreateTagCommand,
  CreateTagUseCase,
  GetEntryCommand,
  GetEntryUseCase,
  GetTagCommand,
  GetTagUseCase,
  PingCommand,
  PingUseCase
}
import solutions.s4y.vocabla.app.repo.{EntryRepository, TagRepository}
import solutions.s4y.vocabla.app.repo.tx.TransactionManager
import solutions.s4y.vocabla.domain.identity.Identifier.given
import solutions.s4y.vocabla.infra.pgsql.InfraPgLive
import zio.{ZIO, ZLayer, durationInt}

final class VocablaApp(
    private val tm: TransactionManager,
    private val entriesRepository: EntryRepository,
    private val tagsRepository: TagRepository
) extends PingUseCase,
      CreateEntryUseCase,
      CreateTagUseCase,
      GetEntryUseCase,
      GetTagUseCase:
  VocablaApp.logger.debug("Creating VocablaApp instance")

  override def apply[R](
      pingCommand: PingCommand
  ): ZIO[Any, String, PingCommand.Response] =
    ZIO
      .succeed("PONG from VocablaApp: " + pingCommand.payload)
      .delay(200.millis)

  override def apply[R](
      command: CreateEntryCommand
  ): ZIO[R, String, CreateEntryCommand.Response] = tm.transaction {
    for {
      entryId <- entriesRepository.create(command.entry)
    } yield CreateEntryCommand.Response(entryId)
  }

  override def apply[R](
      command: CreateTagCommand
  ): ZIO[R, String, CreateTagCommand.Response] = tm.transaction {
    for {
      tagId <- tagsRepository.create(command.tag)
    } yield CreateTagCommand.Response(tagId)
  }

  override def apply[R](
      command: GetEntryCommand
  ): ZIO[R, String, GetEntryCommand.Response] = tm.transaction {
    for {
      entry <- entriesRepository.get(command.entryId)
    } yield GetEntryCommand.Response(entry)
  }

  override def apply[R](
      command: GetTagCommand
  ): ZIO[R, String, GetTagCommand.Response] = tm.transaction {
    for {
      tag <- tagsRepository.get(command.tagId)
    } yield GetTagCommand.Response(tag)
  }
end VocablaApp

object VocablaApp:
  val layer: ZLayer[Any, String, PingUseCase & CreateEntryUseCase & CreateTagUseCase & GetEntryUseCase & GetTagUseCase] =
    InfraPgLive.layer >>> ZLayer.fromFunction(
      new VocablaApp(_, _, _)
    )

  private val logger = LoggerFactory.getLogger(VocablaApp.getClass)
end VocablaApp
