package solutions.s4y.vocabla.domain

import solutions.s4y.vocabla.domain.User
import solutions.s4y.vocabla.domain.identity.Identifier

/** A context the current action is running in. It is mostly used for permission
  * checks.
  */
case class UserContext(id: Identifier[User], user: User):
  val studentId: Identifier[User.Student] = id.asIdentifier[User.Student]
