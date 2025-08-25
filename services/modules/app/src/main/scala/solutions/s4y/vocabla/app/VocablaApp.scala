package solutions.s4y.vocabla.app

import org.slf4j.LoggerFactory
import solutions.s4y.vocabla.app.ports.*
import solutions.s4y.vocabla.app.repo.tx.TransactionManager
import solutions.s4y.vocabla.app.repo.{
  EntryRepository,
  TagRepository,
  UserRepository
}
import solutions.s4y.vocabla.domain.User
import solutions.s4y.vocabla.domain.identity.Identifier
import solutions.s4y.vocabla.infra.pgsql.InfraPgLive
import zio.{ZIO, ZLayer, durationInt}

final class VocablaApp(
    private val tm: TransactionManager,
    private val userRepository: UserRepository,
    private val entriesRepository: EntryRepository,
    private val tagsRepository: TagRepository
) extends PingUseCase,
      GetUserUseCase,
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
    entriesRepository
      .get(command.entryId)
      .map((entry => GetEntryCommand.Response(entry)))
  }

  override def apply[R](
      command: GetTagCommand
  ): ZIO[R, String, GetTagCommand.Response] = tm.transaction {
    tagsRepository
      .get(command.tagId)
      .map(tag => GetTagCommand.Response(tag))
  }

  override def apply[R](
      command: GetUserCommand
  ): ZIO[R, String, GetUserCommand.Response] =
    apply(command.userId).map(userOpt => GetUserCommand.Response(userOpt))

  override def apply[R](
      id: Identifier[User]
  ): ZIO[R, String, Option[User]] = tm.transaction {
    userRepository.get(id)
  }

end VocablaApp

object VocablaApp:
  val layer: ZLayer[
    Any,
    String,
    PingUseCase & GetUserUseCase & CreateEntryUseCase & CreateTagUseCase &
      GetEntryUseCase & GetTagUseCase
  ] =
    InfraPgLive.layer >>> ZLayer.fromFunction(
      new VocablaApp(_, _, _, _)
    )

  private val logger = LoggerFactory.getLogger(VocablaApp.getClass)
end VocablaApp
