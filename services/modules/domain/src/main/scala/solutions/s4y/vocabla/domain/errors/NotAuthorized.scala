package solutions.s4y.vocabla.domain.errors

import solutions.s4y.vocabla.domain.authorizationService.{
  AuthorizedEntity,
  AuthorizedOperation,
  AuthorizedOwner
}

sealed trait NotAuthorized:
  val op: AuthorizedOperation

case class NeitherAdminNotStudent(override val op: AuthorizedOperation)
    extends NotAuthorized
case class NotTheOwner(
    override val op: AuthorizedOperation,
    owned: AuthorizedEntity,
    owner: Option[AuthorizedOwner]
) extends NotAuthorized
