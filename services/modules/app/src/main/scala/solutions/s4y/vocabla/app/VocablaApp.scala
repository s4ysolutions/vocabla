package solutions.s4y.vocabla.app

import org.slf4j.LoggerFactory
import solutions.s4y.vocabla.app.ports.*
import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.app.repo.tx.{Transaction, TransactionContext, TransactionManager}
import solutions.s4y.vocabla.app.repo.{EntryRepository, TagRepository, UserRepository}
import solutions.s4y.vocabla.domain.errors.NotAuthorized
import solutions.s4y.vocabla.domain.identity.Identifier
import solutions.s4y.vocabla.domain.{User, UserContext, authorizationService}
import solutions.s4y.vocabla.infra.pgsql.InfraPgLive
import zio.prelude.Validation
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
  ): ZIO[
    UserContext,
    ServiceFailure | NotAuthorized,
    CreateEntryCommand.Response
  ] =
    authorized {
      authorizationService.canCreateEntry(command.entry, _)
    } *> transaction(entriesRepository.create(command.entry)).map { entryId =>
      CreateEntryCommand.Response(entryId)
    }

  override def apply(
      command: CreateTagCommand
  ): ZIO[
    UserContext,
    ServiceFailure | NotAuthorized,
    CreateTagCommand.Response
  ] = authorized(authorizationService.canCreateTag(command.tag, _)) *>
    transaction(tagsRepository.create(command.tag)).map(tagId =>
      CreateTagCommand.Response(tagId)
    )

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
  ): IO[ServiceFailure, GetUserCommand.Response] =
    apply(command.userId)
      .mapBoth(
        e => ServiceFailure(e),
        userOpt => GetUserCommand.Response(userOpt)
      )

  override def apply(
      id: Identifier[User]
  ): IO[String, Option[User]] = tm.transaction {
    userRepository.get(id)
  }

  private def authorized(
      validate: UserContext => Validation[NotAuthorized, Unit]
  ): ZIO[UserContext, NotAuthorized, Unit] =
    ZIO.serviceWithZIO[UserContext](validate(_).toZIO)

  private def transaction[T](
      unitOfWork: ZIO[TR & TX, String, T]
  ): IO[ServiceFailure, T] =
    tm.transaction(unitOfWork)
      .mapError(e => ServiceFailure(e))

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
