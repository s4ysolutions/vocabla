package solutions.s4y.vocabla.domain

import solutions.s4y.vocabla.domain.owner.Owned
import zio.prelude.Validation

object AuthorizationService:
  def canAssignTag(
      tag: Tag,
      entry: Entry,
      userContext: UserContext
  ): Validation[String, Unit] =
    val tagOwnedByUser = isOwned(tag, userContext)
    if (tag.isOwnedBy(userContext.studentId))
      Validation.succeed(())
    else Validation.fail("Tag is not owned by the user.")
    val entryOwnedByUser =
      if (entry.isOwnedBy(userContext.studentId))
        Validation.succeed(())
      else Validation.fail("Entry is not owned by the user.")
    Validation.validateWith(tagOwnedByUser, entryOwnedByUser)((_, _) => ())

  def canCreateTag(
      tag: Tag,
      userContext: UserContext
  ): Validation[String, Unit] =
    isOwned(tag, userContext)

  def canCreateEntry(
      entry: Entry,
      userContext: UserContext
  ): Validation[String, Unit] =
    isOwned(entry, userContext)

  private def isOwned(
      owned: Owned[User.Student],
      userContext: UserContext
  ): Validation[String, Unit] =
    if userContext.user.isAdmin then return Validation.succeed(())
    if !userContext.user.isStudent then
      return Validation.fail("User is neither admin nor student.")
    if !owned.isOwnedBy(userContext.studentId) then
      return Validation.fail(s"{$owned} is not owned by the user.")
    Validation.succeed(())

end AuthorizationService
