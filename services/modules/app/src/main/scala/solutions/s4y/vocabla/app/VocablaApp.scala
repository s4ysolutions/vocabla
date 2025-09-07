package solutions.s4y.vocabla.app

import org.slf4j.LoggerFactory
import solutions.s4y.vocabla.app.VocablaApp.mapInfraFailure
import solutions.s4y.vocabla.app.ports.*
import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.app.ports.tag_create.{CreateTagRequest, CreateTagResponse, CreateTagUseCase}
import solutions.s4y.vocabla.app.ports.tag_get.{GetTagRequest, GetTagResponse, GetTagUseCase}
import solutions.s4y.vocabla.app.repo.error.InfraFailure
import solutions.s4y.vocabla.app.repo.tx.{TransactionContext, TransactionManager}
import solutions.s4y.vocabla.app.repo.{EntryRepository, TagRepository, UserRepository}
import solutions.s4y.vocabla.domain.errors.NotAuthorized
import solutions.s4y.vocabla.domain.identity.Identifier
import solutions.s4y.vocabla.domain.{User, UserContext, authorizationService}
import zio.prelude.Validation
import zio.{IO, ZIO, ZLayer}

final class VocablaApp[TX <: TransactionContext](
    private val tm: TransactionManager[TX],
    private val userRepository: UserRepository[TX],
    private val entriesRepository: EntryRepository[TX],
    private val tagsRepository: TagRepository[TX]
) extends PingUseCase,
      GetUserUseCase,
      CreateEntryUseCase,
      CreateTagUseCase,
      GetEntryUseCase,
      GetTagUseCase:
  VocablaApp.logger.debug("Creating VocablaApp instance")

  /** **************************************************************************
    * Diagnostic
    */
  override def apply(
      pingCommand: PingCommand
  ): IO[String, PingCommand.Response] =
    ZIO
      .succeed("PONG from VocablaApp: " + pingCommand.payload)
    // .delay(200.millis)

  /** **************************************************************************
    * Entries
    */
  override def apply(
      command: CreateEntryCommand
  ): ZIO[
    UserContext,
    ServiceFailure | NotAuthorized,
    CreateEntryCommand.Response
  ] =
    authorized(
      authorizationService.canCreateEntry(command.entry, _)
    ) *> transaction("entryCreate", entriesRepository.create(command.entry))
      .map(
        CreateEntryCommand.Response(_)
      )

  override def apply(
      command: GetEntryCommand
  ): ZIO[
    UserContext,
    ServiceFailure | NotAuthorized,
    GetEntryCommand.Response
  ] =
    authorized(
      authorizationService.canGetEntry(command.entryId, _)
    ) *> transaction(
      "entryGet",
      entriesRepository
        .get(command.entryId)
    ).map(entry => GetEntryCommand.Response(entry))

  /** **************************************************************************
    * Tags
    */
  override def apply(
      command: CreateTagRequest
  ): ZIO[
    UserContext,
    ServiceFailure | NotAuthorized,
    CreateTagResponse
  ] = authorized(authorizationService.canCreateTag(command.tag, _)) *>
    transaction("tagCreate", tagsRepository.create(command.tag)).map(tagId =>
      CreateTagResponse(tagId)
    )

  override def apply(
      command: GetTagRequest
  ): ZIO[UserContext, ServiceFailure | NotAuthorized, GetTagResponse] =
    authorized(authorizationService.canGetTag(command.tagId, _)) *>
      transaction(
        "tagGet",
        tagsRepository.get(command.tagId)
      ).map(tag => GetTagResponse(tag))

  /** **************************************************************************
    * Users
    */
  override def apply(
      command: GetUserCommand
  ): IO[ServiceFailure, GetUserCommand.Response] =
    apply(command.userId)
      .map(
        GetUserCommand.Response(_)
      )

  override def apply(
      id: Identifier[User]
  ): IO[ServiceFailure, Option[User]] = transaction(
    "userGetById",
    userRepository.get(id)
  ).mapError(f => ServiceFailure(f.message, f.cause))

  /** **************************************************************************
    * privates
    */
  private def authorized(
      validate: UserContext => Validation[NotAuthorized, Unit]
  ): ZIO[UserContext, NotAuthorized, Unit] =
    ZIO.serviceWithZIO[UserContext](validate(_).toZIO)

  private def transaction[R, T](
      log: String,
      unitOfWork: TX ?=> ZIO[R, InfraFailure, T]
  ): ZIO[R, ServiceFailure, T] =
    tm.transaction(log, unitOfWork).mapInfraFailure
end VocablaApp

object VocablaApp:
  def layer[TX <: TransactionContext: zio.Tag](): ZLayer[
    TransactionManager[TX] & UserRepository[TX] &
      (EntryRepository[TX] & TagRepository[TX]),
    Nothing,
    VocablaApp[TX]
  ] =
    ZLayer.fromFunction(
      new VocablaApp[TX](_, _, _, _)
    )

  extension [R, A](self: zio.ZIO[R, InfraFailure, A])
    private def mapInfraFailure: zio.ZIO[R, ServiceFailure, A] =
      self.mapError(f => ServiceFailure(f.message, f.cause))

  private val logger = LoggerFactory.getLogger(VocablaApp.getClass)
end VocablaApp
