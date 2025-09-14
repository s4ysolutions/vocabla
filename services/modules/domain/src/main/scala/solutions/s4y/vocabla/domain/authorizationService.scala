package solutions.s4y.vocabla.domain

import solutions.s4y.vocabla.domain.errors.{
  NeitherAdminNotStudent,
  NotAuthorized,
  NotTheOwner
}
import solutions.s4y.vocabla.domain.identity.Identifier
import solutions.s4y.vocabla.domain.owner.Owned
import zio.prelude.Validation

object authorizationService:
  opaque type AuthorizedOperation = String
  object AuthorizedOperation:
    private def apply(op: String): AuthorizedOperation = op
    private given Conversion[String, AuthorizedOperation] = apply(_)
    extension (op: AuthorizedOperation) def value: String = op
  end AuthorizedOperation

  opaque type AuthorizedEntity = String
  object AuthorizedEntity:
    def apply(entity: String): AuthorizedEntity = entity
    extension (entity: AuthorizedEntity) def value: String = entity
  end AuthorizedEntity

  opaque type AuthorizedOwner = String
  object AuthorizedOwner:
    def apply(owner: String): AuthorizedOwner = owner
    extension (owner: AuthorizedOwner) def value: String = owner
  end AuthorizedOwner

  private final val opAssignTagToEntry: AuthorizedOperation = "AssignTagToEntry"
  def canAssignTag(
      tag: Tag,
      entry: Entry,
      userContext: UserContext
  ): Validation[NotAuthorized, Unit] =
    val tagOwnedByUser = isOwned(tag, userContext, opAssignTagToEntry)
    val entryOwnedByUser =
      if (entry.isOwnedBy(userContext.studentId))
        Validation.succeed(())
      else
        Validation.fail(NotTheOwner(opAssignTagToEntry, entry.toString, None))
    Validation.validateWith(tagOwnedByUser, entryOwnedByUser)((_, _) => ())

  def canCreateTag(
      tag: Tag,
      userContext: UserContext
  ): Validation[NotAuthorized, Unit] =
    isOwned(tag, userContext, "CreateTag")

  def canGetTag(
      tagId: Identifier[Tag],
      userContext: UserContext
  ): Validation[NotAuthorized, Unit] =
    if userContext.user.isAdmin then return Validation.succeed(())
    if !userContext.user.isStudent then
      return Validation.fail(NeitherAdminNotStudent("GetTag"))
    Validation.succeed(())

  def canCreateEntry(
      entry: Entry,
      userContext: UserContext
  ): Validation[NotAuthorized, Unit] =
    isOwned(entry, userContext, "CreateEntry")

  def canGetEntry(
      entryId: Identifier[Entry],
      userContext: UserContext
  ): Validation[NotAuthorized, Unit] =
    if userContext.user.isAdmin then return Validation.succeed(())
    if !userContext.user.isStudent then
      return Validation.fail(NeitherAdminNotStudent("GetEntry"))
    Validation.succeed(())

  def canGetEntries(
      ownerId: Option[Identifier[User]],
      userContext: UserContext
  ): Validation[NotAuthorized, Unit] =
    if userContext.user.isAdmin then return Validation.succeed(())
    if !userContext.user.isStudent then
      return Validation.fail(NeitherAdminNotStudent("GetEntries"))
    
    // If ownerId filter is provided, it must match the current user
    ownerId match {
      case Some(requestOwnerId) if requestOwnerId == userContext.studentId.asInstanceOf[Identifier[User]] =>
        Validation.succeed(())
      case Some(_) =>
        Validation.fail(NotTheOwner("GetEntries", "entries", None))
      case None =>
        Validation.fail(NotTheOwner("GetEntries", "entries must be filtered by ownerId", None))
    }

  private def isOwned(
      owned: Owned[User.Student],
      userContext: UserContext,
      op: AuthorizedOperation
  ): Validation[NotAuthorized, Unit] =
    if userContext.user.isAdmin then return Validation.succeed(())
    if !userContext.user.isStudent then
      return Validation.fail(NeitherAdminNotStudent(op))
    if !owned.isOwnedBy(userContext.studentId) then
      return Validation.fail(NotTheOwner(op, owned.toString, None))
    Validation.succeed(())

end authorizationService
