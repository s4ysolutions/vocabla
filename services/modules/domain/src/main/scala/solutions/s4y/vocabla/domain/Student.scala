package solutions.s4y.vocabla.domain

import solutions.s4y.vocabla.domain.identity.{Identified, IdentifierSchema}
import zio.prelude.Equal
import zio.schema.{DeriveSchema, Schema}

final case class Student(
    nickname: String
):
  override def toString: String = s"Student: $nickname"

object Student:
  given Equal[Student] =
    Equal.make((a, b) => a.nickname == b.nickname)

  given Equal[Identified[Student]] =
    Equal.make((a, b) => a.e.nickname == b.e.nickname)

  given (using is: IdentifierSchema): Schema[Student] =
    DeriveSchema.gen[Student]
