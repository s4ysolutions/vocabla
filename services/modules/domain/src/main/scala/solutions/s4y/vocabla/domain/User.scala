package solutions.s4y.vocabla.domain

import solutions.s4y.vocabla.domain.identity.{
  Identified,
  Identifier,
  IdentifierSchema
}
import zio.prelude.Equal
import zio.schema.{Schema, derived}

final case class User(
    admin: Option[User.Admin],
    student: Option[User.Student]
):
  val isAdmin: Boolean = admin.exists(_.active)
  val isStudent: Boolean = student.isDefined

object User:
  final case class Admin(active: Boolean)
  final case class Student(nickname: String)

  given (using is: IdentifierSchema): Schema[Student] = Schema.derived

  given Equal[Student] =
    Equal.make((a, b) => a.nickname == b.nickname)

  given Equal[Identified[Student]] =
    Equal.make((a, b) => a.e.nickname == b.e.nickname)
