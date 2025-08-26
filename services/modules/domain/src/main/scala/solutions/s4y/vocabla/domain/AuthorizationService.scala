package solutions.s4y.vocabla.domain

import zio.prelude.Validation

object AuthorizationService:
  /** Admin can assign any tag to any entry. Student can assign only their own
    * tags to their own entries.
    * @param tag
    *   a tag to be assigned
    * @param entry
    *   an entry to assign the tag to
    * @param userContext
    *   the context of the user performing the action
    * @return
    *   true if the user can assign the tag to the entry, false otherwise
    */
  def canAssignTag(
      tag: Tag,
      entry: Entry,
      userContext: UserContext
  ): Validation[String, Unit] =
    if (userContext.user.isAdmin)
      return Validation.succeed(())
    if (!userContext.user.isStudent)
      return Validation.fail("User is neither admin not student.")

    val tagOwnedByUser =
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
    if (userContext.user.isAdmin)
      return Validation.succeed(())
    if (!userContext.user.isStudent)
      return Validation.fail("User is neither admin not student.")
    if (!tag.isOwnedBy(userContext.studentId))
      return Validation.fail("Tag is not owned by the user.")
    Validation.succeed(())

end AuthorizationService
