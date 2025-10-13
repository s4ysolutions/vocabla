package solutions.s4y.vocabla.app

import org.slf4j.LoggerFactory
import solutions.s4y.i18n.ResourcesStringsResolver.default
import solutions.s4y.i18n.t
import solutions.s4y.vocabla.app.VocablaApp.mapInfraFailure
import solutions.s4y.vocabla.app.ports.*
import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.app.ports.lang_get.{
  GetLanguagesResponse,
  GetLanguagesUseCase
}
import solutions.s4y.vocabla.app.ports.students.entries.entries_get.{
  GetEntriesCommand,
  GetEntriesResponse,
  GetEntriesUseCase
}
import solutions.s4y.vocabla.app.ports.students.entries.entry_create.{
  CreateEntryCommand,
  CreateEntryResponse,
  CreateEntryUseCase
}
import solutions.s4y.vocabla.app.ports.students.entries.entry_delete.{
  DeleteEntryCommand,
  DeleteEntryResponse,
  DeleteEntryUseCase
}
import solutions.s4y.vocabla.app.ports.students.entries.entry_get.{
  GetEntryCommand,
  GetEntryResponse,
  GetEntryUseCase
}
import solutions.s4y.vocabla.app.ports.students.settings.known_lang.*
import solutions.s4y.vocabla.app.ports.students.settings.learn_lang.*
import solutions.s4y.vocabla.app.ports.students.settings.tags.*
import solutions.s4y.vocabla.app.ports.students.settings.{
  GetLearningSettingsCommand,
  GetLearningSettingsResponse,
  GetLearningSettingsUseCase
}
import solutions.s4y.vocabla.app.repo.*
import solutions.s4y.vocabla.app.repo.error.InfraFailure
import solutions.s4y.vocabla.app.repo.tx.{
  TransactionContext,
  TransactionManager
}
import solutions.s4y.vocabla.domain.errors.{InvalidLangCode, NotAuthorized}
import solutions.s4y.vocabla.domain.identity.Identifier
import solutions.s4y.vocabla.domain.{
  Lang,
  User,
  UserContext,
  authorizationService
}
import zio.prelude.Validation
import zio.{IO, UIO, ZIO, ZLayer}

final class VocablaApp[TX <: TransactionContext](
    private val tm: TransactionManager[TX],
    private val userRepository: UserRepository[TX],
    private val entriesRepository: EntryRepository[TX],
    private val tagsRepository: TagRepository[TX],
    private val langRepository: LangRepository,
    private val learnLanguagesRepository: LearnLanguagesRepository[TX],
    private val knownLanguagesRepository: KnownLanguagesRepository[TX]
) extends PingUseCase,
      GetLearningSettingsUseCase,
      GetLanguagesUseCase,
      GetUserUseCase,
      CreateEntryUseCase,
      GetEntryUseCase,
      GetEntriesUseCase,
      CreateTagUseCase,
      GetTagUseCase,
      DeleteTagUseCase,
      AddLearnLangUseCase,
      AddKnownLangUseCase,
      RemoveLearnLangUseCase,
      RemoveKnownLangUseCase,
      DeleteEntryUseCase:
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
    CreateEntryResponse
  ] =
    authorized(
      authorizationService.canCreateEntry(command.entry, _)
    ) *> transaction("entryCreate", entriesRepository.create(command.entry))
      .map(
        CreateEntryResponse(_)
      )

  override def apply(
      command: GetEntryCommand
  ): ZIO[
    UserContext,
    ServiceFailure | NotAuthorized,
    GetEntryResponse
  ] =
    authorized(
      authorizationService.canGetEntry(command.entryId, command.userId, _)
    ) *> transaction(
      "entryGet",
      entriesRepository
        .get(command.entryId)
    ).map(entry => GetEntryResponse(entry))

  override def apply(
      command: GetEntriesCommand
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
        tagIds = command.tagIds,
        langCodes = command.langs,
        text = command.text
      )
    ).map(entriesMap => GetEntriesResponse(entriesMap))

  override def apply(
      command: DeleteEntryCommand
  ): ZIO[
    UserContext,
    ServiceFailure | NotAuthorized,
    DeleteEntryResponse
  ] =
    authorized(
      authorizationService.canDeleteEntry(command.entryId, command.userId, _)
    ) *> transaction(
      "entryDelete",
      entriesRepository.delete(command.entryId)
    ).map(deleted => DeleteEntryResponse(deleted))

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
    transaction(
      "tagCreate",
      for {
        tagId <- tagsRepository.create(command.tag)
        userContext <- ZIO.service[UserContext]
        settings <- userRepository.getLearningSettings(userContext.studentId)
      } yield CreateTagResponse(tagId, settings)
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
      tagsRepository.delete(command.tagId) *> ZIO
        .serviceWithZIO[UserContext](userContext =>
          userRepository.getLearningSettings(userContext.studentId)
        )
    ).map(settings => DeleteTagResponse(settings))

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

  override def apply(request: GetLearningSettingsCommand): ZIO[
    UserContext,
    ServiceFailure | NotAuthorized,
    GetLearningSettingsResponse
  ] =
    authorized(
      authorizationService.canGetLearningSettings(request.studentId, _)
    ) *>
      transaction(
        "GetLearningSettings",
        userRepository.getLearningSettings(request.studentId)
      ).mapBoth(
        f => ServiceFailure(f.message, f.cause),
        ls => GetLearningSettingsResponse(ls)
      )

  /** **************************************************************************
    * Known Languages
    */

  override def apply(
      command: AddKnownLangCommand
  ): ZIO[
    UserContext,
    ServiceFailure | NotAuthorized | InvalidLangCode,
    AddKnownLangResponse
  ] =
    authorized(
      authorizationService.canChooseKnownLang(command.studentId, _)
    ) *> validateLangCode(command.langCode).toZIO *>
      transaction(
        "addKnownLanguage",
        knownLanguagesRepository.addKnownLanguage(
          command.studentId,
          command.langCode
        ) *>
          ZIO
            .serviceWithZIO[UserContext](userContext =>
              userRepository.getLearningSettings(userContext.studentId)
            )
      ).map(settings => AddKnownLangResponse(settings))

  override def apply(
      command: RemoveKnownLangCommand
  ): ZIO[
    UserContext,
    ServiceFailure | NotAuthorized,
    RemoveKnownLangResponse
  ] =
    authorized(
      authorizationService.canChooseKnownLang(command.studentId, _)
    ) *>
      transaction(
        "removeKnownLanguage",
        knownLanguagesRepository.removeKnownLanguage(
          command.studentId,
          command.langCode
        ) *> ZIO
          .serviceWithZIO[UserContext](userContext =>
            userRepository.getLearningSettings(userContext.studentId)
          )
      ).map(settings => RemoveKnownLangResponse(settings))

  /** **************************************************************************
    * Learn Languages
    */

  override def apply(
      command: AddLearnLangCommand
  ): ZIO[
    UserContext,
    ServiceFailure | NotAuthorized | InvalidLangCode,
    AddLearnLangResponse
  ] =
    authorized(
      authorizationService.canChooseLearnLang(
        command.studentId,
        _
      )
    ) *> validateLangCode(command.langCode).toZIO *>
      transaction(
        "addLearnLanguage",
        learnLanguagesRepository.addLearnLanguage(
          command.studentId,
          command.langCode
        )
          *> ZIO.serviceWithZIO[UserContext](userContext =>
            userRepository.getLearningSettings(userContext.studentId)
          )
      ).map(settings => AddLearnLangResponse(settings))

  override def apply(
      command: RemoveLearnLangCommand
  ): ZIO[
    UserContext,
    ServiceFailure | NotAuthorized,
    RemoveLearnLangResponse
  ] =
    authorized(
      authorizationService.canChooseLearnLang(command.studentId, _)
    ) *>
      transaction(
        "removeLearnLanguage",
        learnLanguagesRepository.removeLearnLanguage(
          command.studentId,
          command.langCode
        ) *> ZIO.serviceWithZIO[UserContext](userContext =>
          userRepository.getLearningSettings(userContext.studentId)
        )
      ).map(settings => RemoveLearnLangResponse(settings))

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
  ): ZIO[R, ServiceFailure, T] = {
    tm.transaction(log, unitOfWork).mapInfraFailure
  }

  private def validateLangCode(
      langCode: Lang.Code
  ): Validation[InvalidLangCode, Unit] =
    if allowedLangCodes.contains(langCode) then Validation.succeed(())
    else
      Validation.fail(
        InvalidLangCode(
          langCode,
          t"Language code $langCode is not in the list of allowed languages"
        )
      )

  private val allowedLangCodes = langRepository.getLangs.map(_.code).toSet
end VocablaApp

object VocablaApp:
  def layer[TX <: TransactionContext: zio.Tag](): ZLayer[
    TransactionManager[TX] & UserRepository[TX] & EntryRepository[TX] &
      TagRepository[TX] & LangRepository & LearnLanguagesRepository[TX] &
      KnownLanguagesRepository[TX],
    Nothing,
    VocablaApp[TX]
  ] =
    ZLayer.fromFunction(
      new VocablaApp[TX](_, _, _, _, _, _, _)
    )

  extension [R, A](self: zio.ZIO[R, InfraFailure, A])
    private def mapInfraFailure: zio.ZIO[R, ServiceFailure, A] =
      self.mapError(f => ServiceFailure(f.message, f.cause))

  private val logger = LoggerFactory.getLogger(VocablaApp.getClass)
end VocablaApp
