package solutions.s4y.vocabla.app

import org.slf4j.LoggerFactory
import solutions.s4y.vocabla.app.ports.*
import solutions.s4y.vocabla.app.ports.errors.{InfraFailure, NotAuthorized}
import solutions.s4y.vocabla.app.repo.tx.{
  Transaction,
  TransactionContext,
  TransactionManager
}
import solutions.s4y.vocabla.app.repo.{
  EntryRepository,
  TagRepository,
  UserRepository
}
import solutions.s4y.vocabla.domain.identity.Identifier
import solutions.s4y.vocabla.domain.{AuthorizationService, User, UserContext}
import solutions.s4y.vocabla.infra.pgsql.InfraPgLive
import zio.{IO, ZIO, ZLayer, durationInt}

final class VocablaApp[TR <: Transaction, TX <: TransactionContext](
    private val tm: TransactionManager[TR, TX],
    val userRepository: UserRepository[TR, TX],
    val entriesRepository: EntryRepository[TR, TX],
    val tagsRepository: TagRepository[TR, TX]
) extends PingUseCase,
      GetUserUseCase,
      CreateEntryUseCase,
      CreateTagUseCase,
      GetEntryUseCase,
      GetTagUseCase:
  VocablaApp.logger.debug("Creating VocablaApp instance")

  override def apply(
      pingCommand: PingCommand
  ): IO[String, PingCommand.Response] =
    ZIO
      .succeed("PONG from VocablaApp: " + pingCommand.payload)
      .delay(200.millis)

  override def apply(
      command: CreateEntryCommand
  ): IO[String, CreateEntryCommand.Response] =
    tm.transaction {
      for {
        entryId <- entriesRepository.create(command.entry)
      } yield CreateEntryCommand.Response(entryId)
    }

  override def apply(
      command: CreateTagCommand
  ): ZIO[
    UserContext,
    InfraFailure | NotAuthorized,
    CreateTagCommand.Response
  ] = for {
    _ <- ZIO.serviceWithZIO[UserContext](
      AuthorizationService
        .canCreateTag(command.tag, _)
        .fold(errors => ZIO.fail(NotAuthorized(errors)), _ => ZIO.unit)
    )
    tagId <- tm
      .transaction { tagsRepository.create(command.tag) }
      .mapError(e => InfraFailure(e))
  } yield CreateTagCommand.Response(tagId)

  override def apply(
      command: GetEntryCommand
  ): IO[String, GetEntryCommand.Response] =
    tm.transaction {
      entriesRepository
        .get(command.entryId)
        .map(entry => GetEntryCommand.Response(entry))
    }

  override def apply(
      command: GetTagCommand
  ): IO[String, GetTagCommand.Response] =
    tm.transaction {
      tagsRepository
        .get(command.tagId)
        .map(tag => GetTagCommand.Response(tag))
    }

  override def apply(
      command: GetUserCommand
  ): IO[InfraFailure, GetUserCommand.Response] =
    apply(command.userId)
      .mapBoth(
        e => InfraFailure(e),
        userOpt => GetUserCommand.Response(userOpt)
      )

  override def apply(
      id: Identifier[User]
  ): IO[String, Option[User]] = tm.transaction {
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
      new VocablaApp[InfraPgLive.TR, InfraPgLive.TX](_, _, _, _)
    )

  private val logger = LoggerFactory.getLogger(VocablaApp.getClass)
end VocablaApp
