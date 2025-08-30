package solutions.s4y.vocabla.domain.errors

import solutions.s4y.i18n.ResourcesStringsResolver.default
import solutions.s4y.i18n.{TranslationTemplate, t}
import solutions.s4y.vocabla.domain.authorizationService.{
  AuthorizedEntity,
  AuthorizedOperation,
  AuthorizedOwner
}

sealed trait NotAuthorized:
  val op: AuthorizedOperation
  val message: TranslationTemplate

case class NeitherAdminNotStudent(override val op: AuthorizedOperation)
    extends NotAuthorized:
  override val message: TranslationTemplate = t"Operation not allowed: $op"

case class NotTheOwner(
    override val op: AuthorizedOperation,
    owned: AuthorizedEntity,
    owner: Option[AuthorizedOwner]
) extends NotAuthorized:
  override val message: TranslationTemplate = owner match
    case Some(o) => t"Operation not allowed: $op, $owned is not owned by $owned"
    case None =>
      t"Operation not allowed: $op, $owned is not owned by the requesting owner"
