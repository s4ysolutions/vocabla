package solutions.s4y.vocabla.app.ports.students.settings

import solutions.s4y.vocabla.domain.Tag
import solutions.s4y.vocabla.domain.User.Student
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import zio.schema.annotation.description
import zio.schema.{DeriveSchema, Schema}

@description("Command to get the learning settings of a student by owner ID.")
final case class GetLearningSettingsCommand(
    @description(
      "ID of the student whose learning settings are to be retrieved."
    )
    studentId: Identifier[Student]
)

object GetLearningSettingsCommand:
  given (using IdentifierSchema): Schema[GetLearningSettingsCommand] =
    DeriveSchema.gen[GetLearningSettingsCommand]
