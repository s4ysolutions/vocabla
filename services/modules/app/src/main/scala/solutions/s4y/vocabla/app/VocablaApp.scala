package solutions.s4y.vocabla.app

import org.slf4j.LoggerFactory
import solutions.s4y.vocabla.app.VocablaApp.mapInfraFailure
import solutions.s4y.vocabla.app.ports.*
import solutions.s4y.vocabla.app.ports.entries_get.{
  GetEntriesRequest,
  GetEntriesResponse,
  GetEntriesUseCase
}
import solutions.s4y.vocabla.app.ports.entry_create.{
  CreateEntryRequest,
  CreateEntryResponse,
  CreateEntryUseCase
}
import solutions.s4y.vocabla.app.ports.entry_get.{
  GetEntryRequest,
  GetEntryResponse,
  GetEntryUseCase
}
import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.app.ports.lang_get.{
  GetLanguagesResponse,
  GetLanguagesUseCase
}
import solutions.s4y.vocabla.app.ports.student_ls_get.{
  GetLearningSettingsRequest,
  GetLearningSettingsResponse,
  GetLearningSettingsUseCase
}
import solutions.s4y.vocabla.app.ports.tag_create.{
  CreateTagCommand,
  CreateTagResponse,
  CreateTagUseCase
}
import solutions.s4y.vocabla.app.ports.tag_delete.{
  DeleteTagCommand,
  DeleteTagResponse,
  DeleteTagUseCase
}
import solutions.s4y.vocabla.app.ports.tag_get.{
  GetTagCommand,
  GetTagResponse,
  GetTagUseCase
}
import solutions.s4y.vocabla.app.repo.error.InfraFailure
import solutions.s4y.vocabla.app.repo.tx.{
  TransactionContext,
  TransactionManager
}
import solutions.s4y.vocabla.app.repo.{
  EntryRepository,
  LangRepository,
  TagRepository,
  UserRepository
}
import solutions.s4y.vocabla.domain.errors.NotAuthorized
import solutions.s4y.vocabla.domain.identity.Identifier
import solutions.s4y.vocabla.domain.{User, UserContext, authorizationService}
import zio.prelude.Validation
import zio.{IO, UIO, ZIO, ZLayer}

final class VocablaApp[TX <: TransactionContext](
    private val tm: TransactionManager[TX],
    private val userRepository: UserRepository[TX],
    private val entriesRepository: EntryRepository[TX],
    private val tagsRepository: TagRepository[TX],
    private val langRepository: LangRepository
) extends PingUseCase,
      GetLearningSettingsUseCase,
      GetLanguagesUseCase,
      GetUserUseCase,
      CreateEntryUseCase,
      GetEntryUseCase,
      GetEntriesUseCase,
      CreateTagUseCase,
      GetTagUseCase,
      DeleteTagUseCase:
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
      command: CreateEntryRequest
  ): ZIO[
    UserContext,
    ServiceFailure | NotAuthorized,
    CreateEntryResponse
  ] =
    authorized(
      authorizationService.canCreateEntry(command.entry, _)
    ) *> transaction("entryCreate", entriesRepository.create(command.entry))
      .map(
        CreateEntryResponse(_)
      )

  override def apply(
      command: GetEntryRequest
  ): ZIO[
    UserContext,
    ServiceFailure | NotAuthorized,
    GetEntryResponse
  ] =
    authorized(
      authorizationService.canGetEntry(command.entryId, _)
    ) *> transaction(
      "entryGet",
      entriesRepository
        .get(command.entryId)
    ).map(entry => GetEntryResponse(entry))

  override def apply(
      command: GetEntriesRequest
  ): ZIO[
    UserContext,
    ServiceFailure | NotAuthorized,
    GetEntriesResponse
  ] =
    authorized(
      authorizationService.canGetEntries(command.ownerId, _)
    ) *> transaction(
      "entriesGet",
      entriesRepository.get(
        ownerId = command.ownerId,
        tagIds = command.tagId,
        langCodes = command.lang,
        text = command.text
      )
    ).map(entriesMap => GetEntriesResponse(entriesMap))

  /** **************************************************************************
    * Tags
    */
  override def apply(
      command: CreateTagCommand
  ): ZIO[
    UserContext,
    ServiceFailure | NotAuthorized,
    CreateTagResponse
  ] = authorized(authorizationService.canCreateTag(command.tag, _)) *>
    transaction("tagCreate", tagsRepository.create(command.tag)).map(tagId =>
      CreateTagResponse(tagId)
    )

  override def apply(
      command: GetTagCommand
  ): ZIO[UserContext, ServiceFailure | NotAuthorized, GetTagResponse] =
    authorized(
      authorizationService.canGetTag(command.ownerId, command.tagId, _)
    ) *>
      transaction(
        "tagGet",
        tagsRepository.get(command.tagId)
      ).map(tag => GetTagResponse(tag))

  override def apply(
      command: DeleteTagCommand
  ): ZIO[UserContext, ServiceFailure | NotAuthorized, DeleteTagResponse] =
    authorized(
      authorizationService.canDeleteTag(command.ownerId, command.tagId, _)
    ) *> transaction(
      "tagDelete",
      tagsRepository.delete(command.tagId).map(r => DeleteTagResponse(r))
    )

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
    * Languages
    */
  override def apply(): UIO[GetLanguagesResponse] = ZIO.succeed(
    GetLanguagesResponse(
      defaultLang = langRepository.defaultLang,
      unknownLang = langRepository.unknownLang,
      languages = langRepository.getLangs
    )
  )

  /** **************************************************************************
    * Learning settings
    */

  override def apply(request: GetLearningSettingsRequest): ZIO[
    UserContext,
    ServiceFailure | NotAuthorized,
    GetLearningSettingsResponse
  ] =
    authorized(
      authorizationService.canGetLearningSettings(request.ownerId, _)
    ) *>
      transaction(
        "GetLearningSettings",
        userRepository.getLearningSettings(request.ownerId)
      ).mapBoth(
        f => ServiceFailure(f.message, f.cause),
        ls => GetLearningSettingsResponse(ls)
      )

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
      (EntryRepository[TX] & TagRepository[TX] & LangRepository),
    Nothing,
    VocablaApp[TX]
  ] =
    ZLayer.fromFunction(
      new VocablaApp[TX](_, _, _, _, _)
    )

  extension [R, A](self: zio.ZIO[R, InfraFailure, A])
    private def mapInfraFailure: zio.ZIO[R, ServiceFailure, A] =
      self.mapError(f => ServiceFailure(f.message, f.cause))

  private val logger = LoggerFactory.getLogger(VocablaApp.getClass)
end VocablaApp
